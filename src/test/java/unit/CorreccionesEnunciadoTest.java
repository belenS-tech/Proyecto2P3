package unit;

import model.*;
import org.junit.jupiter.api.Test;
import repository.RecursoRepository;
import service.*;
import unit.estadisticas.ReservaRepositoryEnMemoria;
import unit.funcionarios.FuncionarioRepositoryEnMemoria;

import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Casos que fallaban en la revisión del enunciado; datos exclusivamente en memoria. */
class CorreccionesEnunciadoTest {
    final CategoriaRecurso categoria = new CategoriaRecurso("C", "Salas");
    final Recurso primero = new Recurso("R", categoria, "Sala uno");
    final Recurso segundo = new Recurso("R2", categoria, "Sala dos");
    final List<Recurso> inventario = new ArrayList<>(List.of(primero, segundo));
    final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria();
    final FuncionarioRepositoryEnMemoria funcionarios = new FuncionarioRepositoryEnMemoria();
    final LocalDate dia = LocalDate.of(2040,1,2);
    final Clock reloj = Clock.fixed(Instant.parse("2040-01-02T12:00:00Z"), ZoneOffset.UTC);
    final RecursoRepository recursos = new RecursoRepository() {
        public List<Recurso> listar() { return new ArrayList<>(inventario); }
        public List<Recurso> listarTodos() { return listar(); }
        public List<Recurso> buscarPorCategoria(String id) { return inventario.stream().filter(r -> r.getCategoria().getId().equals(id)).toList(); }
        public Recurso buscarPorId(String id) { return inventario.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null); }
        public void guardar(Recurso r) { inventario.add(r); }
        public void actualizar(Recurso r) { inventario.removeIf(actual -> actual.getId().equals(r.getId())); inventario.add(r); }
        public void eliminar(Recurso r) { inventario.removeIf(actual -> actual.getId().equals(r.getId())); }
    };

    ReservaService servicio() { return new ReservaService(new DisponibilidadService(recursos, reservas), reservas, reloj); }
    Reserva reserva(String id, LocalTime inicio, LocalTime fin) {
        Reserva r = new Reserva(id, "Reunión", dia, inicio, fin, "F", EstadoReserva.ACTIVA);
        r.setDetalles(new ArrayList<>(List.of(new DetalleReserva(categoria, primero))));
        return r;
    }

    @Test void seleccionAntiguaSeReasignaAlPrimerRecursoQueSigueLibre() {
        Reserva a = reserva("A", LocalTime.of(10,0), LocalTime.of(11,0));
        Reserva b = reserva("B", a.getHoraInicio(), a.getHoraFin());
        servicio().registrar(a); servicio().registrar(b);
        assertEquals("R", a.getDetalles().getFirst().getRecurso().getId());
        assertEquals("R2", b.getDetalles().getFirst().getRecurso().getId());
        assertEquals(2, reservas.listar().size());
    }

    @Test void puedeSolicitarCategoriasSinSeleccionarUnActivoEnElCliente() {
        Reserva r = reserva("A", LocalTime.of(10,0), LocalTime.of(11,0));
        r.getDetalles().getFirst().setRecurso(null);
        servicio().registrar(r);
        assertEquals("R", reservas.buscarPorId("A").getDetalles().getFirst().getRecurso().getId());
    }

    @Test void faltaDeVariasCategoriasNoGuardaParcialmenteYLasIdentificaTodas() {
        Reserva r = reserva("A", LocalTime.of(10,0), LocalTime.of(11,0));
        r.getDetalles().add(new DetalleReserva(new CategoriaRecurso("X", "Proyectores"), null));
        r.getDetalles().add(new DetalleReserva(new CategoriaRecurso("Y", "Computadoras"), null));
        var error = assertThrows(IllegalArgumentException.class, () -> servicio().registrar(r));
        assertTrue(error.getMessage().contains("Proyectores"));
        assertTrue(error.getMessage().contains("Computadoras"));
        assertTrue(reservas.listar().isEmpty());
        assertEquals(3, r.getDetalles().size(), "Conservar selección para corregir e intentar de nuevo");
    }

    @Test void noRepiteCategoriasNiAsignaElMismoRecursoDosVeces() {
        Reserva r = reserva("A", LocalTime.of(10,0), LocalTime.of(11,0));
        r.getDetalles().add(new DetalleReserva(categoria, null));
        assertThrows(IllegalArgumentException.class, () -> servicio().registrar(r));
        assertTrue(reservas.listar().isEmpty());
    }

    @Test void cancelacionValidaDatosPersistidosYRechazaPasadaEnCursoOInicioExacto() {
        for (LocalTime inicio : List.of(LocalTime.of(10,0), LocalTime.of(11,30), LocalTime.of(12,0))) {
            Reserva original = reserva("A" + inicio, inicio, inicio.plusHours(1));
            reservas.guardar(original);
            Reserva manipulada = reserva(original.getId(), LocalTime.of(15,0), LocalTime.of(16,0));
            manipulada.setFecha(dia.plusYears(1));
            assertThrows(IllegalArgumentException.class, () -> servicio().eliminar(manipulada));
            assertSame(original, reservas.buscarPorId(original.getId()));
        }
    }

    @Test void cancelacionDeDiaAnteriorSeRechaza() {
        Reserva original = reserva("PASADA", LocalTime.of(23,0), LocalTime.of(23,30));
        original.setFecha(dia.minusDays(1)); reservas.guardar(original);
        assertThrows(IllegalArgumentException.class, () -> servicio().eliminar(original));
    }

    @Test void cancelarFuturaLiberaTodosSusRecursos() {
        Reserva original = reserva("FUTURA", LocalTime.of(13,0), LocalTime.of(14,0));
        reservas.guardar(original);
        servicio().eliminar(original);
        assertNull(reservas.buscarPorId("FUTURA"));
        assertEquals("R", new DisponibilidadService(recursos, reservas)
                .buscarRecursoDisponible(categoria, dia, original.getHoraInicio(), original.getHoraFin()).getId());
    }

    @Test void actualizarEstadoTampocoPermiteCancelarPasadaConFechaFalsificada() {
        Reserva original = reserva("A", LocalTime.of(10,0), LocalTime.of(11,0)); reservas.guardar(original);
        Reserva manipulada = reserva("A", LocalTime.of(15,0), LocalTime.of(16,0));
        manipulada.setEstado(EstadoReserva.CANCELADA);
        assertThrows(IllegalArgumentException.class, () -> servicio().actualizar(manipulada));
        assertEquals(EstadoReserva.ACTIVA, reservas.buscarPorId("A").getEstado());
    }

    @Test void matricesCubren24HorasMediasHorasYNombreDelFuncionario() {
        funcionarios.guardar(new Funcionario("F", "María Sandoval", "88888888"));
        for (int hora : List.of(0, 5, 10, 22, 23)) {
            Reserva r = reserva("H" + hora, LocalTime.of(hora,0), LocalTime.of(hora,30));
            r.setHoraInicio(LocalTime.of(hora,15)); reservas.guardar(r);
        }
        var calendario = new CalendarizacionRecursosService(recursos, reservas, funcionarios).construirMatriz(dia,"C");
        var actividades = new ProgramacionActividadesService(reservas, funcionarios).construirMatrizSemana(dia);
        assertEquals(24, calendario.getFilas().size()); assertEquals(24, actividades.getFilas().size());
        for (int h = 0; h < 24; h++) {
            boolean ocupada = List.of(0,5,10,22,23).contains(h);
            String valor = calendario.getFilas().get(h).getValor("R");
            String actividad = actividades.getFilas().get(h).getValor(dia);
            assertEquals(ocupada, !valor.isEmpty(), "Calendario hora " + h);
            assertEquals(ocupada, !actividad.isEmpty(), "Actividades hora " + h);
            if (ocupada) { assertTrue(valor.contains("María Sandoval")); assertTrue(actividad.contains("María Sandoval")); }
        }
    }

    @Test void reserva1030a1100ApareceEn10YNoEn11() {
        reservas.guardar(reserva("MEDIA", LocalTime.of(10,30), LocalTime.of(11,0)));
        var calendario = new CalendarizacionRecursosService(recursos,reservas,funcionarios).construirMatriz(dia,"C");
        var actividades = new ProgramacionActividadesService(reservas,funcionarios).construirMatrizSemana(dia);
        assertFalse(calendario.getFilas().get(10).getValor("R").isEmpty());
        assertEquals("", calendario.getFilas().get(11).getValor("R"));
        assertFalse(actividades.getFilas().get(10).getValor(dia).isEmpty());
        assertEquals("", actividades.getFilas().get(11).getValor(dia));
    }

    @Test void dosReservasConsecutivasEnLaMismaHoraSeMuestranAmbasSinIncluirCanceladas() {
        Reserva primera = reserva("A", LocalTime.of(10,0), LocalTime.of(10,30)); primera.setActividad("Primera");
        Reserva segunda = reserva("B", LocalTime.of(10,30), LocalTime.of(11,0)); segunda.setActividad("Segunda");
        Reserva cancelada = reserva("C", LocalTime.of(10,0), LocalTime.of(11,0)); cancelada.setActividad("Cancelada"); cancelada.setEstado(EstadoReserva.CANCELADA);
        reservas.guardar(primera); reservas.guardar(segunda); reservas.guardar(cancelada);
        var calendario = new CalendarizacionRecursosService(recursos,reservas,funcionarios).construirMatriz(dia,"C");
        String celda = calendario.getFilas().get(10).getValor("R");
        assertTrue(celda.contains("Primera")); assertTrue(celda.contains("Segunda")); assertFalse(celda.contains("Cancelada"));
    }

    @Test void estadisticasIncluyenSemanasVaciasYExtremosParcialesDelPeriodo() {
        reservas.guardar(reserva("A", LocalTime.of(10,0), LocalTime.of(11,0)));
        var cantidades = new EstadisticasActividadesService(reservas).calcular(dia, dia.plusWeeks(2));
        assertEquals(List.of(1,0,0), new ArrayList<>(cantidades.values()));
        assertEquals(util.DateUtils.etiquetaSemana(dia), cantidades.keySet().iterator().next());
    }

    @Test void periodoSinReservasIncluyeSemanasConCeroAlCambiarDeAnio() {
        LocalDate desde = LocalDate.of(2039,12,31), hasta = LocalDate.of(2040,1,9);
        var cantidades = new EstadisticasActividadesService(reservas).calcular(desde,hasta);
        assertEquals(3, cantidades.size()); assertTrue(cantidades.values().stream().allMatch(n -> n == 0));
        assertEquals(List.of(0), new ArrayList<>(new EstadisticasActividadesService(reservas).calcular(dia,dia).values()));
    }

    @Test void historialCuentaActivasYFinalizadasYExcluyeCanceladasConsistentemente() {
        funcionarios.guardar(new Funcionario("F","María Sandoval","88888888"));
        int hora = 9;
        for (EstadoReserva estado : EstadoReserva.values()) {
            Reserva r = reserva(estado.name(),LocalTime.of(hora,0),LocalTime.of(hora+1,0));
            r.setEstado(estado); reservas.guardar(r); hora++;
        }
        assertEquals(2,new EstadisticasRecursosService(reservas).contarPorCategoria(dia,dia).get("Salas"));
        assertEquals(2,new EstadisticasActividadesService(reservas).calcular(dia,dia).get(util.DateUtils.etiquetaSemana(dia)));
        var calendario = new CalendarizacionRecursosService(recursos,reservas,funcionarios).construirMatriz(dia,"C");
        var actividades = new ProgramacionActividadesService(reservas,funcionarios).construirMatrizSemana(dia);
        for (Reserva r : reservas.listar()) {
            int fila = r.getHoraInicio().getHour();
            boolean visible = r.getEstado() != EstadoReserva.CANCELADA;
            assertEquals(visible,!calendario.getFilas().get(fila).getValor("R").isEmpty());
            assertEquals(visible,!actividades.getFilas().get(fila).getValor(dia).isEmpty());
        }
    }
}
