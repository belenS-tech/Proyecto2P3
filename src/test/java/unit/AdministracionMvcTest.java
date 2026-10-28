package unit;

import controller.*;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import service.SessionManager;
import view.*;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/** Acciona los controles reales; sustituye únicamente los diálogos modales. */
class AdministracionMvcTest {
    @TempDir Path temporal;
    static class Funcionarios extends FuncionariosView {
        String error, ruta; boolean aceptar;
        @Override public void mostrarMensaje(String texto) {}
        @Override public void mostrarError(String texto) { error = texto; }
        @Override public boolean confirmar(String texto) { return aceptar; }
        @Override public String elegirRutaReporte(String nombre) { return ruta; }
    }
    static class Categorias extends CategoriasView {
        String error, ruta; boolean aceptar;
        @Override public void mostrarMensaje(String texto) {}
        @Override public void mostrarError(String texto) { error = texto; }
        @Override public boolean confirmar(String texto) { return aceptar; }
        @Override public String elegirRutaReporte(String nombre) { return ruta; }
    }
    @AfterEach void cerrarSesion() throws Exception {
        SwingUtilities.invokeAndWait(() -> SessionManager.getInstancia().cerrarSesion());
    }
    static void contienePdf(Path ruta, String esperado) throws Exception {
        var lector = new com.lowagie.text.pdf.PdfReader(ruta.toString());
        try {
            assertTrue(lector.getNumberOfPages() >= 1);
            var texto = new com.lowagie.text.pdf.parser.PdfTextExtractor(lector);
            assertTrue(texto.getTextFromPage(1).contains(esperado));
        } finally { lector.close(); }
    }
    @Test void funcionariosCrudConfirmacionesYExportacionDesdeBoton() throws Exception {
        Path pdf = temporal.resolve("funcionarios.pdf");
        SwingUtilities.invokeAndWait(() -> {
            SessionManager.getInstancia().iniciarSesion(new Usuario("ADMIN", "", Rol.ADMINISTRADOR));
            var empleados = new unit.funcionarios.FuncionarioRepositoryEnMemoria();
            var usuarios = new unit.funcionarios.UsuarioRepositoryEnMemoria();
            var servicio = new service.FuncionarioService(empleados, usuarios,
                    new unit.estadisticas.ReservaRepositoryEnMemoria());
            var vista = new Funcionarios(); vista.ruta = pdf.toString();
            new FuncionariosController(vista, servicio);
            vista.mostrarEnFormulario(new Funcionario("F", "Ana inicial", "88888888"));
            vista.getBotonGuardar().doClick(0); assertNull(vista.error);
            assertEquals("F", usuarios.buscarPorId("F").getClave());
            vista.getTabla().setRowSelectionInterval(0, 0);
            assertEquals("F", vista.getId());
            vista.mostrarEnFormulario(new Funcionario("F", "Ana modificada", "88888889"));
            vista.getBotonGuardar().doClick(0);
            assertEquals("Ana modificada", servicio.buscarPorId("F").getNombre());
            vista.getBotonImprimir().doClick(0); assertNull(vista.error);
            vista.getTabla().setRowSelectionInterval(0, 0);
            vista.getBotonBorrar().doClick(0); assertNotNull(servicio.buscarPorId("F"));
            vista.aceptar = true; vista.getBotonBorrar().doClick(0);
            assertNull(servicio.buscarPorId("F")); assertNull(usuarios.buscarPorId("F"));
            assertNull(vista.error);
        });
        contienePdf(pdf, "Ana modificada");
    }
    @Test void categoriasCrudConfirmacionesYExportacionDesdeBoton() throws Exception {
        Path pdf = temporal.resolve("categorias.pdf");
        SwingUtilities.invokeAndWait(() -> {
            SessionManager.getInstancia().iniciarSesion(new Usuario("ADMIN", "", Rol.ADMINISTRADOR));
            var servicio = new service.CategoriaService(new unit.categorias.CategoriaRepositoryEnMemoria());
            var vista = new Categorias(); vista.ruta = pdf.toString();
            new CategoriasController(vista, servicio);
            vista.mostrarEnFormulario(new CategoriaRecurso("", "Salas iniciales"));
            vista.getBotonGuardar().doClick(0); assertNull(vista.error);
            String id = servicio.listarTodos().getFirst().getId(); assertFalse(id.isBlank());
            vista.getTabla().setRowSelectionInterval(0, 0); assertEquals(id, vista.getId());
            vista.mostrarEnFormulario(new CategoriaRecurso(id, "Salas modificadas"));
            vista.getBotonGuardar().doClick(0);
            assertEquals("Salas modificadas", servicio.buscarPorId(id).getDescripcion());
            vista.getBotonImprimir().doClick(0); assertNull(vista.error);
            vista.getTabla().setRowSelectionInterval(0, 0);
            vista.getBotonBorrar().doClick(0); assertNotNull(servicio.buscarPorId(id));
            vista.aceptar = true; vista.getBotonBorrar().doClick(0);
            assertNull(servicio.buscarPorId(id)); assertNull(vista.error);
        });
        contienePdf(pdf, "Salas modificadas");
    }
}
