package unit;

import controller.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import service.SessionManager;
import view.*;
import javax.swing.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

/** Flujos reales de los paneles Swing separados: consulta, error y exportación. */
class ConsultasMvcTest {
    @TempDir Path temporal;
    final CorreccionesEnunciadoTest datos = new CorreccionesEnunciadoTest();

    @BeforeEach void preparar() {
        datos.funcionarios.guardar(new Funcionario("F","María Sandoval","88888888"));
        Reserva realizada = datos.reserva("FINAL",LocalTime.of(10,45),LocalTime.of(11,15));
        realizada.setEstado(EstadoReserva.FINALIZADA); datos.reservas.guardar(realizada);
    }
    @AfterEach void cerrarSesion() throws Exception {
        SwingUtilities.invokeAndWait(() -> SessionManager.getInstancia().cerrarSesion());
    }
    void iniciarSesion() { SessionManager.getInstancia().iniciarSesion(new Usuario("F","",Rol.FUNCIONARIO)); }
    void fecha(JSpinner spinner, LocalDate fecha) { spinner.setValue(Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant())); }
    void pdfValido(String nombre) throws Exception {
        Path pdf = temporal.resolve(nombre);
        assertTrue(Files.size(pdf) > 1000);
        var lector = new com.lowagie.text.pdf.PdfReader(pdf.toString());
        try { assertTrue(lector.getNumberOfPages() >= 1); } finally { lector.close(); }
    }
    api.CategoriaService categorias() {
        var repo = new unit.categorias.CategoriaRepositoryEnMemoria(); repo.guardar(datos.categoria);
        return new service.CategoriaService(repo);
    }

    @Test void calendarioConsultaHistorialYExportaDespuesDeSepararControlador() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            iniciarSesion();
            var vista = new CalendarizacionRecursosPanel() {
                @Override public String elegirRutaGuardado(String sugerido) { return temporal.resolve("calendario.pdf").toString(); }
            };
            new CalendarizacionRecursosController(vista,categorias(),
                    new service.CalendarizacionRecursosService(datos.recursos,datos.reservas,datos.funcionarios));
            fecha(vista.getFechaSpinner(),datos.dia);
            assertFalse(vista.getBtnImprimir().isEnabled());
            vista.getBtnCargar().doClick(0);
            assertEquals(24,vista.getModeloTabla().getRowCount());
            assertEquals(3,vista.getModeloTabla().getColumnCount());
            assertTrue(vista.getModeloTabla().getValueAt(10,1).toString().contains("María Sandoval"));
            assertFalse(vista.getModeloTabla().isCellEditable(10,1));
            assertTrue(vista.getBtnImprimir().isEnabled()); vista.getBtnImprimir().doClick(0);
            assertTrue(vista.getLblMensaje().getText().startsWith("Reporte generado"));
        });
        pdfValido("calendario.pdf");
    }

    @Test void actividadesMuestraSemanaCompletaEHistorialYExporta() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            iniciarSesion();
            var vista = new ProgramacionActividadesPanel() {
                @Override public String elegirRutaGuardado(String sugerido) { return temporal.resolve("actividades.pdf").toString(); }
            };
            new ProgramacionActividadesController(vista,new service.ProgramacionActividadesService(datos.reservas,datos.funcionarios));
            fecha(vista.getFechaReferenciaSpinner(),datos.dia); vista.getBtnCargar().doClick(0);
            assertEquals(24,vista.getModeloTabla().getRowCount()); assertEquals(8,vista.getModeloTabla().getColumnCount());
            int columna = datos.dia.getDayOfWeek().getValue();
            assertTrue(vista.getModeloTabla().getValueAt(10,columna).toString().contains("María Sandoval"));
            assertTrue(vista.getModeloTabla().getValueAt(11,columna).toString().contains("María Sandoval"));
            assertEquals(util.DateUtils.etiquetaSemana(datos.dia),vista.getLblSemana().getText());
            vista.getBtnImprimir().doClick(0);
            assertTrue(vista.getLblMensaje().getText().startsWith("Reporte generado"));
        });
        pdfValido("actividades.pdf");
    }

    @Test void estadisticasRecursosActualizaTablaYGraficoYExporta() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            iniciarSesion();
            var vista = new EstadisticasRecursosPanel() {
                @Override public String elegirRutaGuardado(String sugerido) { return temporal.resolve("recursos.pdf").toString(); }
            };
            new EstadisticasRecursosController(vista,new service.EstadisticasRecursosService(datos.reservas));
            fecha(vista.getSpnDesde(),datos.dia); fecha(vista.getSpnHasta(),datos.dia);
            vista.getBtnCalcular().doClick(0);
            assertEquals(1,vista.getModeloTabla().getRowCount());
            assertEquals(1,vista.getDataset().getValue("Recurso","Salas"));
            vista.getBtnImprimir().doClick(0);
            assertTrue(vista.getLblMensaje().getText().startsWith("Reporte generado"));
            fecha(vista.getSpnDesde(),datos.dia.plusDays(1)); vista.getBtnCalcular().doClick(0);
            assertFalse(vista.getBtnImprimir().isEnabled());
            assertTrue(vista.getLblMensaje().getText().contains("posterior"));
        });
        pdfValido("recursos.pdf");
    }

    @Test void estadisticasActividadesMantieneCerosGraficoYExportacionConVistaIndependiente() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            iniciarSesion();
            var vista = new EstadisticasActividadesPanel() {
                @Override public String elegirRutaGuardado(String sugerido) { return temporal.resolve("estadisticas.pdf").toString(); }
            };
            new EstadisticasActividadesController(vista,new service.EstadisticasActividadesService(datos.reservas));
            fecha(vista.getFechaDesdeSpinner(),datos.dia); fecha(vista.getFechaHastaSpinner(),datos.dia.plusWeeks(2));
            vista.getBtnCalcular().doClick(0);
            assertEquals(3,vista.getModeloTabla().getRowCount()); assertEquals(3,vista.getDataset().getColumnCount());
            assertEquals(1,vista.getModeloTabla().getValueAt(0,1)); assertEquals(0,vista.getModeloTabla().getValueAt(1,1));
            assertFalse(vista.getModeloTabla().isCellEditable(0,1));
            vista.getBtnImprimir().doClick(0);
            assertTrue(vista.getLblMensaje().getText().startsWith("Reporte generado"));
            fecha(vista.getFechaDesdeSpinner(),datos.dia.plusYears(1)); vista.getBtnCalcular().doClick(0);
            assertFalse(vista.getBtnImprimir().isEnabled()); assertTrue(vista.getLblMensaje().getText().contains("posterior"));
        });
        pdfValido("estadisticas.pdf");
    }

    @Test void consultaFallidaDeshabilitaReporteAnteriorYPermiteReintentar() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var fallar = new AtomicBoolean(false);
            var vista = new ProgramacionActividadesPanel();
            new ProgramacionActividadesController(vista,fecha -> {
                if (fallar.get()) throw new IllegalArgumentException("Servidor desconectado");
                return new service.ProgramacionActividadesService(datos.reservas,datos.funcionarios).construirMatrizSemana(fecha);
            });
            fecha(vista.getFechaReferenciaSpinner(),datos.dia); vista.getBtnCargar().doClick(0);
            assertTrue(vista.getBtnImprimir().isEnabled());
            fallar.set(true); vista.getBtnCargar().doClick(0);
            assertEquals("Servidor desconectado",vista.getLblMensaje().getText()); assertFalse(vista.getBtnImprimir().isEnabled());
            fallar.set(false); vista.getBtnCargar().doClick(0);
            assertTrue(vista.getBtnImprimir().isEnabled());
        });
    }

    @Test void categoriaSinRecursosMuestraEstadoVacioYNoExporta() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var vista = new CalendarizacionRecursosPanel();
            new CalendarizacionRecursosController(vista,categorias(),(fecha,categoria) -> new ResultadoCalendarizacion(List.of(),List.of()));
            vista.getBtnCargar().doClick(0);
            assertEquals(0,vista.getModeloTabla().getRowCount()); assertEquals(1,vista.getModeloTabla().getColumnCount());
            assertFalse(vista.getBtnImprimir().isEnabled()); assertTrue(vista.getLblMensaje().getText().contains("no tiene recursos"));
        });
    }
}
