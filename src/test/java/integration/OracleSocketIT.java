package integration;

import client.ServiceProxy;
import model.*;
import network.Protocol.Alert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import repository.Repositories;
import server.*;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Recorrido real Proxy -> TCP -> servicio -> Oracle, con datos temporales. */
@EnabledIfSystemProperty(named="oracle.it", matches="true")
class OracleSocketIT {
    @Test void flujoCompletoConOracleYDosClientes() throws Exception {
        String key = "IT-" + UUID.randomUUID();
        String categoryId = null, reservationId = null;
        Recurso resource = null;
        BackendServer backend = null;
        Repositories.usuarios().guardar(new Usuario(key,"prueba",Rol.ADMINISTRADOR));
        try {
            backend = new BackendServer("127.0.0.1",0,BackendMain.services());
            BackendServer running = backend;
            Thread.ofVirtual().start(() -> { try { running.serve(); } catch (Exception e) { throw new RuntimeException(e); } });
            try (var admin = new ServiceProxy("127.0.0.1",backend.port()); var employee = new ServiceProxy("127.0.0.1",backend.port())) {
                admin.service(api.AuthService.class).iniciarSesion(key,"prueba");
                var categories = admin.service(api.CategoriaService.class);
                var employees = admin.service(api.FuncionarioService.class);
                var resources = admin.service(api.RecursoService.class);
                categories.incluir(key);
                CategoriaRecurso category = categories.buscar(key).getFirst(); categoryId = category.getId();
                categories.modificar(categoryId,key + " modificada");
                employees.incluir(key+"-f","Funcionario de prueba","88888888");
                employees.modificar(key+"-f","Nombre actualizado","77777777");
                assertEquals("Nombre actualizado",employees.buscarPorId(key+"-f").getNombre());
                resource = new Recurso(key,category,"Sala temporal"); resources.registrar(resource);
                resource.setDescripcion("Sala actualizada"); resources.actualizar(resource);
                assertEquals("Sala actualizada",resources.buscarPorId(key).getDescripcion());
                employee.service(api.AuthService.class).iniciarSesion(key+"-f",key+"-f");
                assertThrows(IllegalArgumentException.class, () -> employee.service(api.FuncionarioService.class).listarTodos());
                assertThrows(IllegalArgumentException.class, () -> employee.service(api.FuncionarioService.class).buscarPorId(key+"-f"));
                assertThrows(IllegalArgumentException.class, () -> employee.service(api.RecursoService.class).listarTodos());
                assertTrue(employee.service(api.CategoriaService.class).listarTodos().stream().anyMatch(c -> c.getId().equals(category.getId())));
                employee.service(api.AuthService.class).cambiarClave(key+"-f","nueva","nueva");
                BlockingQueue<Alert> alerts = new LinkedBlockingQueue<>(); admin.addAlertListener(alerts::add);
                LocalDate day = LocalDate.of(2040,1,2);
                Reserva reservation = new Reserva("","Actividad temporal",day,LocalTime.of(5,30),LocalTime.of(6,0),key+"-f",EstadoReserva.ACTIVA);
                reservation.getDetalles().add(new DetalleReserva(category,null));
                var reservations = employee.service(api.ReservaService.class);
                reservations.registrar(reservation);
                Alert alert = alerts.poll(5,TimeUnit.SECONDS); assertNotNull(alert); reservationId = alert.reserva().getId();
                Reserva stored = Repositories.reservas().buscarPorId(reservationId);
                assertNotNull(stored); assertEquals(key,stored.getDetalles().getFirst().getRecurso().getId());
                var calendar = admin.service(api.CalendarizacionRecursosService.class).construirMatriz(day,categoryId);
                assertEquals(24,calendar.getFilas().size());
                assertEquals("Actividad temporal - Nombre actualizado",calendar.getFilas().get(5).getValor(key));
                var activities = admin.service(api.ProgramacionActividadesService.class).construirMatrizSemana(day);
                assertEquals(24,activities.getFilas().size());
                assertEquals("Actividad temporal (Nombre actualizado)",activities.getFilas().get(5).getValor(day));
                var statistics = admin.service(api.EstadisticasActividadesService.class).calcular(day,day.plusWeeks(2));
                assertEquals(3,statistics.size());
                assertEquals(0,statistics.get(util.DateUtils.etiquetaSemana(day.plusWeeks(1))));
                assertEquals(0,statistics.get(util.DateUtils.etiquetaSemana(day.plusWeeks(2))));
                assertFalse(admin.service(api.EstadisticasRecursosService.class).contarPorCategoria(day,day).isEmpty());
                stored.setEstado(EstadoReserva.FINALIZADA); reservations.actualizar(stored);
                assertEquals(EstadoReserva.FINALIZADA,reservations.buscarPorId(reservationId).getEstado());
                assertEquals("Actividad temporal - Nombre actualizado",admin.service(api.CalendarizacionRecursosService.class)
                        .construirMatriz(day,categoryId).getFilas().get(5).getValor(key));
                assertEquals("Actividad temporal (Nombre actualizado)",admin.service(api.ProgramacionActividadesService.class)
                        .construirMatrizSemana(day).getFilas().get(5).getValor(day));
                assertTrue(admin.service(api.EstadisticasActividadesService.class).calcular(day,day)
                        .get(util.DateUtils.etiquetaSemana(day)) >= 1);
                stored.setEstado(EstadoReserva.CANCELADA); reservations.actualizar(stored);
                assertEquals(EstadoReserva.CANCELADA,reservations.buscarPorId(reservationId).getEstado());
                reservations.eliminar(stored); assertNull(reservations.buscarPorId(reservationId));
                resources.eliminar(resource); categories.eliminar(categoryId); employees.eliminar(key+"-f");
                assertNull(Repositories.funcionarios().buscarPorId(key+"-f"));
                assertNull(Repositories.usuarios().buscarPorId(key+"-f"));
            }
        } finally {
            if (backend != null) backend.close();
            if (reservationId != null) {
                Reserva r = Repositories.reservas().buscarPorId(reservationId); if (r != null) Repositories.reservas().eliminar(r);
            }
            if (resource != null && Repositories.recursos().buscarPorId(key) != null) Repositories.recursos().eliminar(resource);
            if (categoryId != null && Repositories.categorias().buscarPorId(categoryId) != null) Repositories.categorias().eliminar(categoryId);
            Repositories.funcionarios().eliminar(key+"-f");
            Repositories.usuarios().eliminar(key+"-f"); Repositories.usuarios().eliminar(key);
        }
    }
}
