package controller;

import model.Recurso;
import model.CategoriaRecurso;
import report.PdfReportService;
import report.ReportHeader;
import report.ReportTable;
import api.RecursoService;
import api.CategoriaService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class RecursoController {

    private final view.RecursoView vista;

    private final CategoriaService categoriaService;
    private final RecursoService recursoService;

    // Filtro
    private final JComboBox<CategoriaRecurso> cmbFiltroCategoria;
    private final JTextField txtFiltroDescripcion;
    private final JButton btnBuscar;
    private final JButton btnImprimir;

    // Formulario
    private final JTextField txtId;
    private final JComboBox<CategoriaRecurso> cmbCategoria;
    private final JTextField txtDescripcion;
    private final JButton btnAgregar;
    private final JButton btnModificar;
    private final JButton btnEliminar;
    private final JButton btnLimpiar;
    private final JButton btnConsultar;
    private final JTable tabla;
    private final DefaultTableModel modeloTabla;

    public RecursoController(view.RecursoView vista, CategoriaService categoriaService) {
        this(vista, categoriaService, client.Services.get(RecursoService.class));
    }

    public RecursoController(view.RecursoView vista, CategoriaService categoriaService, RecursoService recursoService) {
        this.vista = vista;
        this.cmbFiltroCategoria = vista.getCmbFiltroCategoria();
        this.txtFiltroDescripcion = vista.getTxtFiltroDescripcion();
        this.btnBuscar = vista.getBtnBuscar();
        this.btnImprimir = vista.getBtnImprimir();
        this.txtId = vista.getTxtId();
        this.cmbCategoria = vista.getCmbCategoria();
        this.txtDescripcion = vista.getTxtDescripcion();
        this.btnAgregar = vista.getBtnAgregar();
        this.btnModificar = vista.getBtnModificar();
        this.btnEliminar = vista.getBtnEliminar();
        this.btnLimpiar = vista.getBtnLimpiar();
        this.btnConsultar = vista.getBtnConsultar();
        this.tabla = vista.getTabla();
        this.modeloTabla = vista.getModeloTabla();

        this.categoriaService = categoriaService;
        this.recursoService = recursoService;
        cargarCategorias();
        cargarTabla(recursoService.listarTodos());
        registrarEventos();
        vista.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (!vista.isShowing()) return;
                cargarCategorias();
            }
        });
    }

    private void registrarEventos() {
        btnBuscar.addActionListener(e -> buscar());
        btnImprimir.addActionListener(e -> imprimir());
        btnAgregar.addActionListener(e -> guardar());
        btnModificar.addActionListener(e -> modificarRecurso());
        btnEliminar.addActionListener(e -> eliminarRecurso());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        tabla.getSelectionModel().addListSelectionListener(this::alSeleccionarFila);
    }

    private void cargarCategorias() {
        List<CategoriaRecurso> cats = categoriaService.listarTodos();

        cmbFiltroCategoria.removeAllItems();
        cmbFiltroCategoria.addItem(new CategoriaRecurso("", "-- Todas --"));
        for (CategoriaRecurso c : cats) cmbFiltroCategoria.addItem(c);

        cmbCategoria.removeAllItems();
        for (CategoriaRecurso c : cats) cmbCategoria.addItem(c);
    }

    private void cargarTabla(List<Recurso> recursos) {
        modeloTabla.setRowCount(0);
        for (Recurso r : recursos) {
            modeloTabla.addRow(new Object[]{
                    r.getId(),
                    r.getCategoria() != null ? r.getCategoria().getDescripcion() : "",
                    r.getDescripcion()
            });
        }
    }

    private void buscar() {
        CategoriaRecurso catFiltro = (CategoriaRecurso) cmbFiltroCategoria.getSelectedItem();
        String descFiltro = txtFiltroDescripcion.getText().trim().toLowerCase();

        List<Recurso> todos = recursoService.listarTodos();
        List<Recurso> filtrados = todos.stream()
                .filter(r -> catFiltro == null || catFiltro.getId().isBlank()
                        || (r.getCategoria() != null && catFiltro.getId().equals(r.getCategoria().getId())))
                .filter(r -> descFiltro.isBlank()
                        || r.getDescripcion().toLowerCase().contains(descFiltro))
                .toList();

        cargarTabla(filtrados);
    }

    private void imprimir() {
        CategoriaRecurso catFiltro = (CategoriaRecurso) cmbFiltroCategoria.getSelectedItem();
        String descFiltro = txtFiltroDescripcion.getText().trim().toLowerCase();

        List<Recurso> todos = recursoService.listarTodos();
        List<Recurso> filtrados = todos.stream()
                .filter(r -> catFiltro == null || catFiltro.getId().isBlank()
                        || (r.getCategoria() != null && catFiltro.getId().equals(r.getCategoria().getId())))
                .filter(r -> descFiltro.isBlank()
                        || r.getDescripcion().toLowerCase().contains(descFiltro))
                .toList();

        try {
            ReportTable tabla = new ReportTable(List.of("ID", "Categoría", "Descripción"));
            for (Recurso r : filtrados) {
                tabla.agregarFila(r.getId(),
                        r.getCategoria() != null ? r.getCategoria().getDescripcion() : "",
                        r.getDescripcion());
            }
            String usuario = SessionManager.getInstancia().getUsuarioActual().getId();
            ReportHeader header = new ReportHeader("Listado de Recursos", usuario, null);

            String ruta = vista.elegirRutaReporte("recursos.pdf");
            if (ruta == null) return;

            new PdfReportService().generarReporteTabla(ruta, header, tabla);
            vista.mostrarMensaje("Reporte generado: " + ruta);
        } catch (Exception ex) {
            vista.mostrarError("Error al generar el reporte: " + ex.getMessage());
        }
    }

    private void alSeleccionarFila(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        txtId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        // buscar categoría por descripción para seleccionarla en el combo
        String descCat = String.valueOf(modeloTabla.getValueAt(fila, 1));
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getDescripcion().equals(descCat)) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
        txtDescripcion.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
    }

    private Recurso construirRecurso() {
        CategoriaRecurso cat = (CategoriaRecurso) cmbCategoria.getSelectedItem();
        if (cat == null) throw new IllegalArgumentException("Debe seleccionar una categoría.");
        return new Recurso(txtId.getText(), cat, txtDescripcion.getText());
    }

    private void guardar() {
        try {
            recursoService.registrar(construirRecurso());
            cargarTabla(recursoService.listarTodos());
            limpiarCampos();
            vista.mostrarMensaje("Recurso guardado correctamente.");
        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void modificarRecurso() {
        try {
            recursoService.actualizar(construirRecurso());
            cargarTabla(recursoService.listarTodos());
            vista.mostrarMensaje("Recurso modificado correctamente.");
        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void eliminarRecurso() {
        try {
            recursoService.eliminar(construirRecurso());
            cargarTabla(recursoService.listarTodos());
            limpiarCampos();
            vista.mostrarMensaje("Recurso eliminado correctamente.");
        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtDescripcion.setText("");
        if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
        tabla.clearSelection();
    }
}
