package unit.estadisticas;

import model.CategoriaRecurso;
import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.EstadisticasRecursosService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasRecursosServiceTest {

    private ReservaRepositoryEnMemoria reservas;
    private EstadisticasRecursosService servicio;

    private CategoriaRecurso sala;
    private CategoriaRecurso laptop;

    @BeforeEach
    void prepararCadaPrueba() {
        reservas = new ReservaRepositoryEnMemoria();
        servicio = new EstadisticasRecursosService(reservas);

        sala = new CategoriaRecurso("CAT-000001", "Sala para 10 personas");
        laptop = new CategoriaRecurso("CAT-000002", "Laptop windows 11");
    }

    private Reserva crearReserva(String id, LocalDate fecha, EstadoReserva estado,
                                 CategoriaRecurso... categorias) {
        Reserva reserva = new Reserva(id, "Reunion", fecha,
                LocalTime.of(8, 0), LocalTime.of(10, 0), "111", estado);

        for (CategoriaRecurso categoria : categorias) {
            Recurso recurso = new Recurso("R-" + categoria.getId(), categoria, "Recurso de prueba");
            reserva.getDetalles().add(new DetalleReserva(categoria, recurso));
        }
        return reserva;
    }

    @Test
    void cuentaUnaReservaPorCategoria() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 5), EstadoReserva.ACTIVA, sala));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get("Sala para 10 personas"));
    }

    @Test
    void acumulaVariasReservasDeLaMismaCategoria() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 5), EstadoReserva.ACTIVA, laptop));
        reservas.guardar(crearReserva("RES-000002",
                LocalDate.of(2026, 8, 6), EstadoReserva.ACTIVA, laptop));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(2, resultado.get("Laptop windows 11"));
    }

    @Test
    void cuentaVariasCategoriasDeUnaMismaReserva() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 5), EstadoReserva.ACTIVA, sala, laptop));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get("Sala para 10 personas"));
        assertEquals(1, resultado.get("Laptop windows 11"));
    }

    @Test
    void ignoraLasReservasCanceladas() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 5), EstadoReserva.ACTIVA, sala));
        reservas.guardar(crearReserva("RES-000002",
                LocalDate.of(2026, 8, 6), EstadoReserva.CANCELADA, sala));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(1, resultado.get("Sala para 10 personas"));
    }

    @Test
    void incluyeLasReservasFinalizadas() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 5), EstadoReserva.FINALIZADA, sala));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(1, resultado.get("Sala para 10 personas"));
    }

    @Test
    void excluyeLasReservasFueraDelPeriodo() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 7, 31), EstadoReserva.ACTIVA, sala));
        reservas.guardar(crearReserva("RES-000002",
                LocalDate.of(2026, 9, 1), EstadoReserva.ACTIVA, sala));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void incluyeLasFechasLimiteDelPeriodo() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 1), EstadoReserva.ACTIVA, sala));
        reservas.guardar(crearReserva("RES-000002",
                LocalDate.of(2026, 8, 31), EstadoReserva.ACTIVA, sala));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(2, resultado.get("Sala para 10 personas"));
    }

    @Test
    void devuelveVacioSiNoHayReservas() {
        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void funcionaCuandoDesdeYHastaSonElMismoDia() {
        reservas.guardar(crearReserva("RES-000001",
                LocalDate.of(2026, 8, 5), EstadoReserva.ACTIVA, sala));

        Map<String, Integer> resultado = servicio.contarPorCategoria(
                LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 5));

        assertEquals(1, resultado.get("Sala para 10 personas"));
    }

    @Test
    void rechazaFechasNulas() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.contarPorCategoria(null, LocalDate.of(2026, 8, 31)));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.contarPorCategoria(LocalDate.of(2026, 8, 1), null));
    }

    @Test
    void rechazaUnPeriodoInvertido() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.contarPorCategoria(
                        LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 1)));
    }
}