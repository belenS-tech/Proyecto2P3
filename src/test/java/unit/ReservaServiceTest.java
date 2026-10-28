package unit;

import org.junit.jupiter.api.Test;
import service.ReservaService;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaServiceTest {
    @Test
    void noPermiteReprogramarReservaPasadaOYaIniciadaParaLuegoCancelarla() {
        var repo = new unit.estadisticas.ReservaRepositoryEnMemoria();
        var ahora = java.time.LocalDateTime.of(2030, 1, 2, 10, 0);
        var clock = java.time.Clock.fixed(ahora.toInstant(java.time.ZoneOffset.UTC), java.time.ZoneOffset.UTC);
        var servicio = new ReservaService(null, repo, clock);
        for (var inicio : java.util.List.of(ahora.minusDays(1), ahora.minusMinutes(1), ahora)) {
            var guardada = reserva("R", inicio);
            repo.guardar(guardada);
            var manipulada = reserva("R", ahora.plusDays(1));
            assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(manipulada));
            assertEquals(inicio.toLocalDate(), repo.buscarPorId("R").getFecha());
            assertEquals(inicio.toLocalTime(), repo.buscarPorId("R").getHoraInicio());
            assertThrows(IllegalArgumentException.class, () -> servicio.eliminar(manipulada));
            repo.eliminar(guardada);
        }
    }

    @Test
    void permiteReprogramarYCancelarReservaQueAunNoHaIniciado() {
        var repo = new unit.estadisticas.ReservaRepositoryEnMemoria();
        var ahora = java.time.LocalDateTime.of(2030, 1, 2, 10, 0);
        var clock = java.time.Clock.fixed(ahora.toInstant(java.time.ZoneOffset.UTC), java.time.ZoneOffset.UTC);
        var servicio = new ReservaService(null, repo, clock);
        repo.guardar(reserva("R", ahora.plusMinutes(1)));
        var cambio = reserva("R", ahora.plusDays(1));
        servicio.actualizar(cambio);
        assertEquals(cambio.getFecha(), repo.buscarPorId("R").getFecha());
        servicio.eliminar(cambio);
        assertNull(repo.buscarPorId("R"));
    }

    private model.Reserva reserva(String id, java.time.LocalDateTime inicio) {
        var categoria = new model.CategoriaRecurso("C", "Salas");
        var reserva = new model.Reserva(id, "Reunión", inicio.toLocalDate(), inicio.toLocalTime(),
                inicio.toLocalTime().plusHours(1), "a", model.EstadoReserva.ACTIVA);
        reserva.getDetalles().add(new model.DetalleReserva(categoria, new model.Recurso("R", categoria, "Sala")));
        return reserva;
    }

    @Test
    void buscarReservaPorIdNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService recursoService = new ReservaService();
            recursoService.buscarPorId(null);
        });
    }

    @Test
    void registrarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null);
            reservaService.registrar(null);
        });
    }

    @Test
    void actualizarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null);
            reservaService.actualizar(null);
        });
    }

    @Test
    void eliminarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null);
            reservaService.eliminar(null);
        });
    }
}
