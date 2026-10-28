package integration;

import database.Database;
import model.*;
import repository.*;
import service.FuncionarioService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas contra Oracle real, habilitadas explícitamente con -Doracle.it=true. */
@EnabledIfSystemProperty(named="oracle.it", matches="true")
class OracleRepositoryIT {
    private final String id = "IT-" + UUID.randomUUID();
    private final CategoriaRepository categorias = Repositories.categorias();
    private final RecursoRepository recursos = Repositories.recursos();
    private final ReservaRepository reservas = Repositories.reservas();
    private final UsuarioRepository usuarios = Repositories.usuarios();
    private final FuncionarioRepository funcionarios = Repositories.funcionarios();
    private CategoriaRecurso categoria;
    private Recurso recurso;

    @BeforeEach void crear() {
        Database.transaction(c -> {
            usuarios.guardar(new Usuario(id,"inicial",Rol.FUNCIONARIO));
            funcionarios.guardar(new Funcionario(id,"Prueba Oracle","12345678"));
            categoria = new CategoriaRecurso(id,"Categoría de prueba");
            categorias.guardar(categoria);
            recurso = new Recurso(id,categoria,"Recurso de prueba");
            recursos.guardar(recurso);
            return null;
        });
    }

    @AfterEach void limpiar() {
        Database.transaction(c -> {
            for (String reservaId : new String[]{id,id+"-2"}) {
                Reserva r=reservas.buscarPorId(reservaId);
                if(r!=null) reservas.eliminar(r);
            }
            recursos.eliminar(recurso);
            categorias.eliminar(id);
            funcionarios.eliminar(id);
            usuarios.eliminar(id);
            return null;
        });
    }

    private Reserva reserva(String key) {
        Reserva r = new Reserva(key,"Prueba con acentos: reunión",LocalDate.of(2040,1,2),LocalTime.of(10,0),LocalTime.of(11,0),id,EstadoReserva.ACTIVA);
        r.getDetalles().add(new DetalleReserva(categoria,recurso));
        return r;
    }

    @Test void crudYRelacionesPersistenEnNuevasConexiones() {
        categorias.guardar(new CategoriaRecurso(id,"Categoría actualizada"));
        assertEquals("Categoría actualizada",new CategoriaJdbcRepository().buscarPorId(id).getDescripcion());
        recursos.actualizar(new Recurso(id,categoria,"Actualizado"));
        assertEquals("Actualizado",new RecursoJdbcRepository().buscarPorId(id).getDescripcion());
        assertTrue(recursos.buscarPorCategoria(id).stream().anyMatch(r -> r.getId().equals(id)));
        usuarios.guardar(new Usuario(id,"modificada",Rol.FUNCIONARIO));
        assertEquals("modificada",new UsuarioJdbcRepository().buscarPorId(id).getClave());
        funcionarios.guardar(new Funcionario(id,"Nombre actualizado","88888888"));
        assertEquals("Nombre actualizado",new FuncionarioJdbcRepository().buscarPorId(id).getNombre());
        reservas.guardar(reserva(id));
        Reserva leida=new ReservaJdbcRepository().buscarPorId(id);
        assertEquals("Prueba con acentos: reunión",leida.getActividad());
        assertEquals(LocalTime.of(10,0),leida.getHoraInicio());
        assertEquals(id,leida.getDetalles().getFirst().getRecurso().getId());
        leida.setEstado(EstadoReserva.CANCELADA);
        reservas.actualizar(leida);
        assertEquals(EstadoReserva.CANCELADA,reservas.buscarPorId(id).getEstado());
        reservas.eliminar(leida);
        assertNull(reservas.buscarPorId(id));
    }

    @Test void errorEnDetalleRevierteReservaCompleta() {
        Reserva r=reserva(id);
        r.getDetalles().add(new DetalleReserva(categoria,recurso));
        assertThrows(IllegalArgumentException.class,() -> reservas.guardar(r));
        assertNull(reservas.buscarPorId(id));
    }

    @Test void noPermiteCrucesNiEliminarRecursosReservados() {
        reservas.guardar(reserva(id));
        assertThrows(IllegalArgumentException.class,() -> reservas.guardar(reserva(id+"-2")));
        assertNull(reservas.buscarPorId(id+"-2"));
        assertThrows(IllegalArgumentException.class,() -> recursos.eliminar(recurso));
        assertNotNull(recursos.buscarPorId(id));
    }

    @Test void errorRevierteTransaccionDeVariosRepositorios() {
        assertThrows(IllegalArgumentException.class,() -> Database.transaction(c -> {
            usuarios.guardar(new Usuario(id,"no-debe-quedar",Rol.FUNCIONARIO));
            recursos.guardar(recurso); // ID duplicado, debe revertir también la clave.
            return null;
        }));
        assertEquals("inicial",usuarios.buscarPorId(id).getClave());
    }

    @Test void eliminarFuncionarioEliminaUsuarioYReservasAtomicamente() {
        reservas.guardar(reserva(id));
        new FuncionarioService(funcionarios,usuarios,reservas).eliminar(id);
        assertNull(funcionarios.buscarPorId(id));
        assertNull(usuarios.buscarPorId(id));
        assertNull(reservas.buscarPorId(id));
    }

    @Test void consultasConcurrentesReutilizanConexionesSinBloqueosAnidados() {
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            try (var pool = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                java.util.List<java.util.concurrent.Future<?>> jobs = new java.util.ArrayList<>();
                for (int i=0; i<12; i++) jobs.add(pool.submit(() -> {
                    for (int j=0; j<5; j++) assertNotNull(recursos.buscarPorId(id));
                }));
                for (var job : jobs) job.get(12,java.util.concurrent.TimeUnit.SECONDS);
            }
        });
    }

    @Test void transaccionDentroDeConsultaRevierteYComparteConexion() {
        Database.use(connection -> {
            assertSame(connection, Database.use(nested -> nested));
            assertThrows(IllegalArgumentException.class, () -> Database.transaction(c -> {
                assertSame(connection,c);
                usuarios.guardar(new Usuario(id,"no-guardar",Rol.FUNCIONARIO));
                throw new IllegalArgumentException("Revertir");
            }));
            assertTrue(connection.getAutoCommit());
            return null;
        });
        assertEquals("inicial",usuarios.buscarPorId(id).getClave());
    }

    @Test void asignacionDeCategoriasUsaOtroRecursoLibreYSePersisteEnOracle() {
        Recurso segundo = new Recurso(id+"-2", categoria, "Segundo recurso temporal");
        recursos.guardar(segundo);
        try {
            var servicio = new service.ReservaService(new service.DisponibilidadService(recursos,reservas), reservas);
            Reserva primera = reserva(id), seleccionAntigua = reserva(id+"-2");
            servicio.registrar(primera);
            servicio.registrar(seleccionAntigua);
            assertNotEquals(reservas.buscarPorId(id).getDetalles().getFirst().getRecurso().getId(),
                    reservas.buscarPorId(id+"-2").getDetalles().getFirst().getRecurso().getId());
        } finally {
            for (String key : new String[]{id,id+"-2"}) {
                Reserva r = reservas.buscarPorId(key); if (r != null) reservas.eliminar(r);
            }
            recursos.eliminar(segundo);
        }
    }

    @Test void cancelarPasadaValidaFechaGuardadaEnOracle() {
        Reserva pasada = reserva(id); pasada.setFecha(LocalDate.of(2020,1,1)); reservas.guardar(pasada);
        Reserva manipulada = reserva(id);
        var servicio = new service.ReservaService(new service.DisponibilidadService(recursos,reservas), reservas);
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminar(manipulada));
        assertEquals(LocalDate.of(2020,1,1), reservas.buscarPorId(id).getFecha());
    }
}
