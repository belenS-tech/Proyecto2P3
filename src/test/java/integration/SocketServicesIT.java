package integration;

import client.ServiceProxy;
import model.*;
import network.Protocol.Alert;
import org.junit.jupiter.api.*;
import repository.*;
import server.BackendServer;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del protocolo real TCP; no requieren Oracle ni alteran XML. */
class SocketServicesIT {
    BackendServer server;
    final List<ServiceProxy> connections = new ArrayList<>();
    final CategoriaRecurso category = new CategoriaRecurso("C", "Salas");
    final Recurso resource = new Recurso("R", category, "Sala principal");
    final List<Recurso> inventory = new ArrayList<>(List.of(resource));
    final unit.funcionarios.FuncionarioRepositoryEnMemoria employees = new unit.funcionarios.FuncionarioRepositoryEnMemoria();
    final LocalDate day = LocalDate.of(2040, 1, 2);
    final unit.login.UsuarioRepositoryEnMemoria users = new unit.login.UsuarioRepositoryEnMemoria();
    final ReservaRepository reservations = new unit.estadisticas.ReservaRepositoryEnMemoria();
    final RecursoRepository resources = new RecursoRepository() {
        public List<Recurso> listar() { return new ArrayList<>(inventory); }
        public List<Recurso> listarTodos() { return listar(); }
        public List<Recurso> buscarPorCategoria(String id) { return id.equals("C") ? listar() : new ArrayList<>(); }
        public Recurso buscarPorId(String id) { return inventory.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null); }
        public void guardar(Recurso r) { }
        public void actualizar(Recurso r) { }
        public void eliminar(Recurso r) { }
    };
    @BeforeEach void start() throws Exception {
        employees.guardar(new Funcionario("a", "Ana López", "88888888"));
        employees.guardar(new Funcionario("b", "Beatriz Mora", "77777777"));
        users.guardar(new Usuario("a", "a", Rol.FUNCIONARIO));
        users.guardar(new Usuario("b", "b", Rol.FUNCIONARIO));
        users.guardar(new Usuario("admin", "admin", Rol.ADMINISTRADOR));
        var categories = new unit.categorias.CategoriaRepositoryEnMemoria();
        categories.guardar(category);
        server = new BackendServer("127.0.0.1", 0, Map.of(
                api.AuthService.class, new service.AuthService(users),
                api.FuncionarioService.class, new service.FuncionarioService(employees, users, reservations),
                api.CategoriaService.class, new service.CategoriaService(categories, resources, reservations),
                api.ReservaService.class, new service.ReservaService(new service.DisponibilidadService(resources, reservations), reservations),
                api.RecursoService.class, new service.RecursoService(resources),
                api.CalendarizacionRecursosService.class, new service.CalendarizacionRecursosService(resources, reservations, employees),
                api.ProgramacionActividadesService.class, new service.ProgramacionActividadesService(reservations, employees),
                api.EstadisticasRecursosService.class, new service.EstadisticasRecursosService(reservations),
                api.EstadisticasActividadesService.class, new service.EstadisticasActividadesService(reservations)));
        Thread.ofVirtual().start(() -> { try { server.serve(); } catch (Exception e) { throw new RuntimeException(e); } });
    }
    @AfterEach void stop() throws Exception { connections.forEach(ServiceProxy::close); server.close(); }
    ServiceProxy connect(String id) throws Exception {
        ServiceProxy p = new ServiceProxy("127.0.0.1", server.port()); connections.add(p);
        if (id != null) {
            Usuario user = p.service(api.AuthService.class).iniciarSesion(id, id);
            assertEquals("", user.getClave(), "No transportar la contraseña almacenada");
        }
        return p;
    }
    Reserva reservation(String owner, int hour) {
        Reserva r = new Reserva("", "Reunión con acentos", day, LocalTime.of(hour,0), LocalTime.of(hour+1,0), owner, EstadoReserva.ACTIVA);
        r.getDetalles().add(new DetalleReserva(category, resource)); return r;
    }
    @Test void reservaNotificaSoloOtrosUsuariosActivosYPermiteConsultarMatrices() throws Exception {
        var author = connect("a"); var recipient = connect("b"); var sameUser = connect("a"); var anonymous = connect(null);
        BlockingQueue<Alert> alerts = new LinkedBlockingQueue<>(), own = new LinkedBlockingQueue<>(), same = new LinkedBlockingQueue<>(), anon = new LinkedBlockingQueue<>();
        author.addAlertListener(own::add); recipient.addAlertListener(alerts::add); sameUser.addAlertListener(same::add); anonymous.addAlertListener(anon::add);
        author.service(api.ReservaService.class).registrar(reservation("a",10));
        Alert alert = alerts.poll(3, TimeUnit.SECONDS); assertNotNull(alert);
        assertEquals("a", alert.author()); assertEquals("RES-000001", alert.reserva().getId());
        assertEquals("Sala principal", alert.reserva().getDetalles().getFirst().getRecurso().getDescripcion());
        assertNull(own.poll(100, TimeUnit.MILLISECONDS)); assertNull(same.poll(100, TimeUnit.MILLISECONDS)); assertNull(anon.poll(100, TimeUnit.MILLISECONDS));
        assertEquals(1, recipient.service(api.ReservaService.class).listarTodos().size());
        var matrix = recipient.service(api.CalendarizacionRecursosService.class).construirMatriz(day,"C");
        assertFalse(matrix.estaVacio()); assertTrue(matrix.getFilas().stream().anyMatch(f -> !f.getValor("R").isEmpty()));
        assertEquals(7, recipient.service(api.ProgramacionActividadesService.class).construirMatrizSemana(day).getDias().size());
        assertEquals(1, recipient.service(api.EstadisticasRecursosService.class).contarPorCategoria(day,day).get("Salas"));
    }
    @Test void errorDeDisponibilidadNoGuardaNiNotificaYConexionSigueUtilizable() throws Exception {
        var a = connect("a"); var b = connect("b");
        BlockingQueue<Alert> alerts = new LinkedBlockingQueue<>(); a.addAlertListener(alerts::add);
        a.service(api.ReservaService.class).registrar(reservation("a",10));
        assertThrows(IllegalArgumentException.class, () -> b.service(api.ReservaService.class).registrar(reservation("b",10)));
        assertNull(alerts.poll(150, TimeUnit.MILLISECONDS));
        assertEquals(1, b.service(api.ReservaService.class).listarTodos().size());
        b.service(api.ReservaService.class).registrar(reservation("b",11));
        assertNotNull(alerts.poll(3, TimeUnit.SECONDS));
        assertEquals(2, b.service(api.ReservaService.class).listarTodos().size());
    }
    @Test void sesionesPermisosCambioClaveYCierreSonIndependientes() throws Exception {
        var a = connect("a"); var b = connect("b"); var anonymous = connect(null);
        assertThrows(IllegalArgumentException.class, () -> anonymous.service(api.RecursoService.class).listarTodos());
        assertThrows(IllegalArgumentException.class, () -> a.service(api.RecursoService.class).eliminar(resource));
        assertThrows(IllegalArgumentException.class, () -> a.service(api.ReservaService.class).registrar(reservation("b",10)));
        a.service(api.AuthService.class).cambiarClave("a","nueva","nueva");
        b.service(api.AuthService.class).cambiarClave("b","otra","otra");
        assertEquals("nueva", users.buscarPorId("a").getClave()); assertEquals("otra", users.buscarPorId("b").getClave());
        a.logout(); assertThrows(IllegalArgumentException.class, () -> a.service(api.ReservaService.class).listarTodos());
        assertEquals(1, b.service(api.CategoriaService.class).listarTodos().size());
        assertThrows(IllegalArgumentException.class, () -> a.service(api.AuthService.class).iniciarSesion("a","mal"));
        assertEquals("a", a.service(api.AuthService.class).iniciarSesion("a","nueva").getId());
    }
    @Test void noPermiteEliminarReservaAjenaAunqueSeFalsifiqueElFuncionario() throws Exception {
        var a = connect("a"); var b = connect("b");
        a.service(api.ReservaService.class).registrar(reservation("a",10));
        Reserva forged = b.service(api.ReservaService.class).listarTodos().getFirst(); forged.setFuncionarioId("b");
        assertThrows(IllegalArgumentException.class, () -> b.service(api.ReservaService.class).eliminar(forged));
        assertEquals(1, a.service(api.ReservaService.class).listarTodos().size());
    }
    @Test void llamadasConcurrentesSobreUnSocketRecibenSuPropiaRespuesta() throws Exception {
        var a = connect("admin");
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> results = new ArrayList<>();
            for (int i=0; i<20; i++) results.add(pool.submit(() -> a.service(api.RecursoService.class).buscarPorId("R").getId()));
            for (Future<String> f : results) assertEquals("R", f.get(5, TimeUnit.SECONDS));
        }
    }
    @Test void dosClientesConcurrentesObtienenIdsDiferentes() throws Exception {
        var a = connect("a"); var b = connect("b");
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = pool.submit(() -> a.service(api.ReservaService.class).registrar(reservation("a",10)));
            Future<?> second = pool.submit(() -> b.service(api.ReservaService.class).registrar(reservation("b",11)));
            first.get(5, TimeUnit.SECONDS); second.get(5, TimeUnit.SECONDS);
        }
        assertEquals(Set.of("RES-000001","RES-000002"), new HashSet<>(a.service(api.ReservaService.class).listarTodos().stream().map(Reserva::getId).toList()));
    }
    @Test void desconexionDelServidorDespiertaSolicitudes() throws Exception {
        var a = connect("a"); server.close();
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> assertThrows(IllegalArgumentException.class,
                () -> a.service(api.ReservaService.class).listarTodos()));
    }

    @Test void dosClientesConSeleccionAntiguaReservanDistintosRecursosYAlertanLaAsignacionReal() throws Exception {
        inventory.add(new Recurso("R2", category, "Sala alternativa"));
        var a = connect("a"); var b = connect("b");
        BlockingQueue<Alert> alerts = new LinkedBlockingQueue<>();
        a.addAlertListener(alerts::add); b.addAlertListener(alerts::add);
        // Ambos clientes conservan la misma selección antigua R.
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = pool.submit(() -> a.service(api.ReservaService.class).registrar(reservation("a",10)));
            var second = pool.submit(() -> b.service(api.ReservaService.class).registrar(reservation("b",10)));
            first.get(5, TimeUnit.SECONDS); second.get(5, TimeUnit.SECONDS);
        }
        var stored = a.service(api.ReservaService.class).listarTodos();
        assertEquals(2, stored.size());
        assertEquals(Set.of("R", "R2"), stored.stream()
                .map(r -> r.getDetalles().getFirst().getRecurso().getId()).collect(java.util.stream.Collectors.toSet()));
        Alert firstAlert = alerts.poll(3, TimeUnit.SECONDS), secondAlert = alerts.poll(3, TimeUnit.SECONDS);
        assertNotNull(firstAlert); assertNotNull(secondAlert);
        assertEquals(Set.of("R", "R2"), Set.of(firstAlert.reserva().getDetalles().getFirst().getRecurso().getId(),
                secondAlert.reserva().getDetalles().getFirst().getRecurso().getId()));
    }

    @Test void clienteEnviaSoloCategoriasYRecibeMatricesConNombreYMediaHora() throws Exception {
        var a = connect("a"); var b = connect("b");
        Reserva request = reservation("a",5);
        request.setHoraInicio(LocalTime.of(5,30));
        request.setDetalles(List.of(new DetalleReserva(category, null)));
        a.service(api.ReservaService.class).registrar(request);
        var calendar = b.service(api.CalendarizacionRecursosService.class).construirMatriz(day,"C");
        assertEquals(24, calendar.getFilas().size());
        assertEquals("Reunión con acentos - Ana López", calendar.getFilas().get(5).getValor("R"));
        var activities = b.service(api.ProgramacionActividadesService.class).construirMatrizSemana(day);
        assertEquals(24, activities.getFilas().size());
        assertEquals("Reunión con acentos (Ana López)", activities.getFilas().get(5).getValor(day));
    }

    @Test void rechazaCancelacionRemotaPasadaAunqueClienteFalsifiqueFechaFutura() throws Exception {
        var a = connect("a");
        Reserva past = reservation("a",10); past.setId("PAST"); past.setFecha(LocalDate.of(2020,1,1));
        reservations.guardar(past);
        Reserva forged = reservation("a",10); forged.setId("PAST");
        assertThrows(IllegalArgumentException.class, () -> a.service(api.ReservaService.class).eliminar(forged));
        assertNotNull(reservations.buscarPorId("PAST"));
    }

    @Test void noPermiteReprogramarReservaPasadaParaEludirCancelacionPorSocket() throws Exception {
        var a = connect("a");
        Reserva past = reservation("a",10); past.setId("PAST"); past.setFecha(LocalDate.of(2020,1,1));
        reservations.guardar(past);
        var service = a.service(api.ReservaService.class);
        Reserva forged = service.buscarPorId("PAST"); forged.setFecha(day);
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(forged));
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(forged));
        assertEquals(LocalDate.of(2020,1,1), service.buscarPorId("PAST").getFecha());
    }

    @Test void eliminarFuncionarioRevocaTodasSusConexionesYAlertasSinAfectarOtrosUsuarios() throws Exception {
        var a = connect("a"); var same = connect("a"); var b = connect("b"); var admin = connect("admin");
        BlockingQueue<Alert> removed = new LinkedBlockingQueue<>(), other = new LinkedBlockingQueue<>();
        a.addAlertListener(removed::add); same.addAlertListener(removed::add); admin.addAlertListener(other::add);
        admin.service(api.FuncionarioService.class).eliminar(" a ");
        assertNull(users.buscarPorId("a"));
        for (var revoked : List.of(a, same)) {
            assertThrows(IllegalArgumentException.class, () -> revoked.service(api.ReservaService.class).listarTodos());
            assertThrows(IllegalArgumentException.class, () -> revoked.service(api.AuthService.class).cambiarClave("a", "nueva", "nueva"));
        }
        b.service(api.ReservaService.class).registrar(reservation("b",11));
        assertNotNull(other.poll(3, TimeUnit.SECONDS));
        assertNull(removed.poll(200, TimeUnit.MILLISECONDS));
        assertEquals(1, b.service(api.ReservaService.class).listarTodos().size());
        // Reutilizar el ID no debe reactivar conexiones de la cuenta anterior.
        admin.service(api.FuncionarioService.class).incluir("a", "Otra persona", "88888888");
        assertThrows(IllegalArgumentException.class, () -> a.service(api.ReservaService.class).listarTodos());
        assertEquals(1, connect("a").service(api.ReservaService.class).listarTodos().size());
    }

    @Test void eliminarFuncionarioInexistenteNoRevocaSesionesValidas() throws Exception {
        var a = connect("a"); var admin = connect("admin");
        assertThrows(IllegalArgumentException.class, () -> admin.service(api.FuncionarioService.class).eliminar("inexistente"));
        assertNotNull(a.service(api.ReservaService.class).listarTodos());
    }

    @Test void consultasAdministrativasSeRestringenEnServidorYLasCategoriasSiguenDisponibles() throws Exception {
        var employee = connect("a"); var admin = connect("admin"); var anonymous = connect(null);
        for (var denied : List.of(employee, anonymous)) {
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.FuncionarioService.class).listarTodos());
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.FuncionarioService.class).buscar("a", ""));
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.FuncionarioService.class).buscarPorId("a"));
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.RecursoService.class).listarTodos());
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.RecursoService.class).buscarPorId("R"));
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.CategoriaService.class).buscar("Salas"));
            assertThrows(IllegalArgumentException.class, () -> denied.service(api.CategoriaService.class).buscarPorId("C"));
        }
        assertEquals(2, admin.service(api.FuncionarioService.class).listarTodos().size());
        assertEquals("Ana López",admin.service(api.FuncionarioService.class).buscarPorId("a").getNombre());
        assertEquals("R",admin.service(api.RecursoService.class).buscarPorId("R").getId());
        assertEquals(1,admin.service(api.CategoriaService.class).buscar("Salas").size());
        assertEquals(1,employee.service(api.CategoriaService.class).listarTodos().size());
        employee.service(api.ReservaService.class).registrar(reservation("a",10));
        assertFalse(employee.service(api.CalendarizacionRecursosService.class).construirMatriz(day,"C").estaVacio());
        assertEquals(7,employee.service(api.ProgramacionActividadesService.class).construirMatrizSemana(day).getDias().size());
    }

    @Test void historialFinalizadoSeConsultaPorProxyYSoloExcluyeCanceladas() throws Exception {
        Reserva completed = reservation("a",10); completed.setId("FINAL"); completed.setEstado(EstadoReserva.FINALIZADA);
        Reserva cancelled = reservation("b",11); cancelled.setId("CANCEL"); cancelled.setEstado(EstadoReserva.CANCELADA);
        reservations.guardar(completed); reservations.guardar(cancelled);
        for (var viewer : List.of(connect("a"),connect("admin"))) {
            assertEquals(1,viewer.service(api.EstadisticasActividadesService.class).calcular(day,day).get(util.DateUtils.etiquetaSemana(day)));
            assertEquals(1,viewer.service(api.EstadisticasRecursosService.class).contarPorCategoria(day,day).get("Salas"));
            var calendar = viewer.service(api.CalendarizacionRecursosService.class).construirMatriz(day,"C");
            assertTrue(calendar.getFilas().get(10).getValor("R").contains("Ana López"));
            assertEquals("",calendar.getFilas().get(11).getValor("R"));
            var activities = viewer.service(api.ProgramacionActividadesService.class).construirMatrizSemana(day);
            assertTrue(activities.getFilas().get(10).getValor(day).contains("Ana López"));
            assertEquals("",activities.getFilas().get(11).getValor(day));
        }
    }
}
