package controller;

import model.CategoriaRecurso;
import model.Usuario;
import report.PdfReportService;
import report.ReportHeader;
import report.ReportTable;
import api.CategoriaService;
import service.SessionManager;
import view.CategoriasView;

import javax.swing.event.ListSelectionEvent;
import java.util.List;

public class CategoriasController {

    private final CategoriasView vista;
    private final CategoriaService servicio;

    public CategoriasController(CategoriasView vista, CategoriaService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        registrarEventos();
        cargarListado();
    }

    private void registrarEventos() {
        vista.getBotonBuscar().addActionListener(e -> buscar());
        vista.getBotonGuardar().addActionListener(e -> guardar());
        vista.getBotonBorrar().addActionListener(e -> borrar());
        vista.getBotonLimpiar().addActionListener(e -> vista.limpiarFormulario());
        vista.getBotonImprimir().addActionListener(e -> imprimir());

        vista.getTabla().getSelectionModel().addListSelectionListener(this::alSeleccionarFila);
    }

    private void alSeleccionarFila(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        String id = vista.getIdSeleccionado();
        if (id == null) {
            return;
        }
        CategoriaRecurso categoria = servicio.buscarPorId(id);
        if (categoria != null) {
            vista.mostrarEnFormulario(categoria);
        }
    }

    private void cargarListado() {
        vista.cargarTabla(servicio.listarTodos());
    }

    private void buscar() {
        vista.cargarTabla(servicio.buscar(vista.getBuscarDescripcion()));
    }

    /**
     * Si hay un ID en el formulario se modifica; si está vacío,
     * se incluye una categoría nueva con ID autogenerado.
     */
    private void guardar() {
        try {
            String id = vista.getId();

            if (id != null && !id.isBlank()) {
                servicio.modificar(id, vista.getDescripcion());
                vista.mostrarMensaje("Categoría modificada correctamente.");
            } else {
                servicio.incluir(vista.getDescripcion());
                vista.mostrarMensaje("Categoría incluida correctamente.");
            }

            vista.limpiarFormulario();
            cargarListado();

        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void borrar() {
        try {
            String id = vista.getId();
            if (id == null || id.isBlank()) {
                vista.mostrarError("Seleccione una categoría del listado.");
                return;
            }

            if (!vista.confirmar("¿Desea borrar la categoría " + id + "?")) {
                return;
            }

            servicio.eliminar(id);
            vista.mostrarMensaje("Categoría borrada correctamente.");
            vista.limpiarFormulario();
            cargarListado();

        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    /** Genera el reporte PDF respetando el filtro de búsqueda aplicado. */
    private void imprimir() {
        try {
            List<CategoriaRecurso> listado = servicio.buscar(vista.getBuscarDescripcion());

            ReportTable tabla = new ReportTable(List.of("Id", "Descripcion"));
            for (CategoriaRecurso c : listado)
                tabla.agregarFila(c.getId(), c.getDescripcion());

            Usuario usuarioActivo = SessionManager.getInstancia().getUsuarioActual();
            String nombreUsuario = usuarioActivo != null ? usuarioActivo.getId() : "";
            String filtro = vista.getBuscarDescripcion();
            String filtros = (filtro == null || filtro.isBlank()) ? "" : "Descripción: " + filtro.trim();
            ReportHeader header = new ReportHeader("Listado de Categorias", nombreUsuario, filtros);

            String ruta = vista.elegirRutaReporte("categorias.pdf");
            if (ruta == null) return;

            new PdfReportService().generarReporteTabla(ruta, header, tabla);
            vista.mostrarMensaje("Reporte generado: " + ruta);

        } catch (Exception ex) {
            vista.mostrarError("No se pudo generar el reporte: " + ex.getMessage());
        }
    }
}