package unit;

import controller.RecursoController;
import controller.ReservaController;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import service.SessionManager;
import view.RecursoView;
import view.ReservaView;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.time.ZoneId;
import java.util.Date;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Ejercita los controles Swing en el EDT sin ventanas modales ni servidor. */
class VistasMvcTest {
    @org.junit.jupiter.api.io.TempDir java.nio.file.Path temporal;

    @Test void botonesExportanRecursosYReservasConDatosRealesDelListado() throws Exception {
        var recursosPdf = temporal.resolve("recursos.pdf");
        var reservasPdf = temporal.resolve("reservas.pdf");
        SwingUtilities.invokeAndWait(() -> {
            var datos = new CorreccionesEnunciadoTest();
            datos.reservas.guardar(datos.reserva("PDF", LocalTime.of(13,0), LocalTime.of(14,0)));
            SessionManager.getInstancia().iniciarSesion(new Usuario("F", "", Rol.FUNCIONARIO));
            var recursos = new VistaRecursos() {
                @Override public String elegirRutaReporte(String nombre) { return recursosPdf.toString(); }
            };
            new RecursoController(recursos, categorias(datos.categoria), new service.RecursoService(datos.recursos));
            recursos.getBtnImprimir().doClick(0); assertNull(recursos.error);
            var reservas = new VistaReservas() {
                @Override public String elegirRutaReporte(String nombre) { return reservasPdf.toString(); }
            };
            new ReservaController(reservas, categorias(datos.categoria), datos.servicio(), frase -> null);
            reservas.getBtnImprimir().doClick(0); assertNull(reservas.error);
        });
        AdministracionMvcTest.contienePdf(recursosPdf, "Sala uno");
        AdministracionMvcTest.contienePdf(reservasPdf, "Reunión");
    }

    static class VistaRecursos extends RecursoView {
        String error, mensaje;
        @Override public void mostrarMensaje(String texto) { mensaje = texto; }
        @Override public void mostrarError(String texto) { error = texto; }
    }
    static class VistaReservas extends ReservaView {
        String error, mensaje;
        @Override public void mostrarMensaje(String texto) { mensaje = texto; }
        @Override public void mostrarError(String texto) { error = texto; }
    }
    @AfterEach void cerrarSesion() throws Exception {
        SessionManager.getInstancia().cerrarSesion();
        SwingUtilities.invokeAndWait(() -> SessionManager.getInstancia().cerrarSesion());
    }

    service.CategoriaService categorias(CategoriaRecurso categoria) {
        var repo = new unit.categorias.CategoriaRepositoryEnMemoria(); repo.guardar(categoria);
        return new service.CategoriaService(repo);
    }

    @Test void recursosConservaEventosCrudBusquedaYSeleccionConVistaSeparada() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(Component.class.isAssignableFrom(RecursoController.class));
            var datos = new CorreccionesEnunciadoTest();
            VistaRecursos vista = new VistaRecursos();
            new RecursoController(vista, categorias(datos.categoria), new service.RecursoService(datos.recursos));
            assertEquals(2, vista.getModeloTabla().getRowCount());
            vista.getTxtId().setText("NUEVO"); vista.getTxtDescripcion().setText("Nueva sala");
            vista.getBtnAgregar().doClick(0);
            assertNotNull(datos.recursos.buscarPorId("NUEVO")); assertNull(vista.error);
            assertEquals(3,vista.getModeloTabla().getRowCount());
            vista.getTabla().setRowSelectionInterval(2,2);
            assertEquals("NUEVO",vista.getTxtId().getText());
            vista.getTxtDescripcion().setText("Sala modificada"); vista.getBtnModificar().doClick(0);
            assertEquals("Sala modificada",datos.recursos.buscarPorId("NUEVO").getDescripcion());
            vista.getTxtFiltroDescripcion().setText("Sala modificada"); vista.getBtnBuscar().doClick(0);
            assertEquals(1,vista.getModeloTabla().getRowCount());
            vista.getTabla().setRowSelectionInterval(0,0); vista.getBtnEliminar().doClick(0);
            assertNull(datos.recursos.buscarPorId("NUEVO"));
            assertEquals("",vista.getTxtId().getText());
            vista.getTxtId().setText("R"); vista.getTxtDescripcion().setText("No borrar texto");
            vista.getBtnAgregar().doClick(0);
            assertNotNull(vista.error); assertEquals("No borrar texto",vista.getTxtDescripcion().getText());
        });
    }

    @Test void reservasSolicitaCategoriasActualizaListadoYConservaFormularioAnteError() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertFalse(Component.class.isAssignableFrom(ReservaController.class));
            var datos = new CorreccionesEnunciadoTest();
            SessionManager.getInstancia().iniciarSesion(new Usuario("F","",Rol.FUNCIONARIO));
            VistaReservas vista = new VistaReservas();
            var servicio = new service.ReservaService(new service.DisponibilidadService(datos.recursos,datos.reservas),datos.reservas,datos.reloj) {
                @Override public void registrar(Reserva r) {
                    assertNull(r.getDetalles().getFirst().getRecurso(), "El cliente solo elige categorías");
                    r.setId("NUEVA"); super.registrar(r);
                }
            };
            new ReservaController(vista,categorias(datos.categoria),servicio,frase -> null);
            vista.getTxtActividad().setText("Reunión desde la vista");
            vista.getSpnFecha().setValue(Date.from(datos.dia.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            vista.getCmbHoraInicio().setSelectedItem("13:30"); vista.getCmbHoraFin().setSelectedItem("14:00");
            vista.getListCategorias().setSelectedIndex(0); vista.getBtnReservar().doClick(0);
            assertNull(vista.error); assertNotNull(datos.reservas.buscarPorId("NUEVA"));
            assertEquals(1,vista.getModeloTabla().getRowCount()); assertEquals("",vista.getTxtActividad().getText());
            vista.getTabla().setRowSelectionInterval(0,0);
            assertEquals("Reunión desde la vista",vista.getTxtActividad().getText());
            vista.getBtnCancelar().doClick(0);
            assertEquals(0,vista.getModeloTabla().getRowCount()); assertTrue(datos.reservas.listar().isEmpty());
            vista.getTxtActividad().setText("Conservar actividad"); vista.getBtnReservar().doClick(0);
            assertNotNull(vista.error); assertEquals("Conservar actividad",vista.getTxtActividad().getText());
        });
    }

    @Test void extraccionIaConservaMinutosExactosAlLlenarGuardarYVolverASeleccionar() throws Exception {
        CountDownLatch completada = new CountDownLatch(1);
        AtomicReference<VistaReservas> vistaRef = new AtomicReference<>();
        var datos = new CorreccionesEnunciadoTest();
        SwingUtilities.invokeAndWait(() -> {
            SessionManager.getInstancia().iniciarSesion(new Usuario("F","",Rol.FUNCIONARIO));
            VistaReservas vista = new VistaReservas() {
                @Override public void mostrarMensaje(String texto) {
                    super.mostrarMensaje(texto);
                    if (texto.startsWith("Datos extraídos")) completada.countDown();
                }
                @Override public void mostrarError(String texto) { super.mostrarError(texto); completada.countDown(); }
            };
            vistaRef.set(vista);
            var servicio = new service.ReservaService(new service.DisponibilidadService(datos.recursos,datos.reservas),datos.reservas,datos.reloj) {
                @Override public void registrar(Reserva r) { r.setId("EXACTA"); super.registrar(r); }
            };
            new ReservaController(vista,categorias(datos.categoria),servicio,frase -> new service.AiReservationResponse(
                    "Actividad precisa",datos.dia,LocalTime.of(10,45),LocalTime.of(11,17),List.of(datos.categoria)));
            vista.getTxtFrase().setText("Reunión de 10:45 a 11:17");
            vista.getBtnExtraerIA().doClick(0);
        });
        assertTrue(completada.await(5,TimeUnit.SECONDS),"Esperar aplicación de la respuesta en el EDT");
        SwingUtilities.invokeAndWait(() -> {
            VistaReservas vista = vistaRef.get(); assertNull(vista.error);
            assertEquals("10:45",vista.getCmbHoraInicio().getSelectedItem());
            assertEquals("11:17",vista.getCmbHoraFin().getSelectedItem());
            assertTrue(vista.getBtnExtraerIA().isEnabled());
            vista.getBtnReservar().doClick(0); assertNull(vista.error);
            Reserva guardada = datos.reservas.buscarPorId("EXACTA"); assertNotNull(guardada);
            assertEquals(LocalTime.of(10,45),guardada.getHoraInicio()); assertEquals(LocalTime.of(11,17),guardada.getHoraFin());
            int inicioItems = vista.getCmbHoraInicio().getItemCount(), finItems = vista.getCmbHoraFin().getItemCount();
            vista.getTabla().setRowSelectionInterval(0,0);
            assertEquals("10:45",vista.getCmbHoraInicio().getSelectedItem()); assertEquals("11:17",vista.getCmbHoraFin().getSelectedItem());
            assertEquals(inicioItems,vista.getCmbHoraInicio().getItemCount()); assertEquals(finItems,vista.getCmbHoraFin().getItemCount());
            SessionManager.getInstancia().cerrarSesion();
        });
    }
}
