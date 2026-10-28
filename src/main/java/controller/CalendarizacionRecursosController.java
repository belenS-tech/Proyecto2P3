package controller;

import api.*;
import model.*;
import view.*;
import report.*;
import service.SessionManager;
import util.DateUtils;
import javax.swing.*;
import java.awt.Color;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Coordina eventos, consultas remotas y exportación de CalendarizacionRecursosPanel. */
public final class CalendarizacionRecursosController {
    private final CalendarizacionRecursosPanel vista;
    private final CalendarizacionRecursosService service;
    private final CategoriaService categoriaService;
    private final PdfReportService pdfReportService = new PdfReportService();

    private ResultadoCalendarizacion ultimoResultado;
    private LocalDate ultimaFecha;
    private CategoriaRecurso ultimaCategoria;

    private final JSpinner fechaSpinner;
    private final JComboBox<CategoriaRecurso> categoriaCombo;
    private final JButton btnCargar;
    private final JButton btnImprimir;
    private final JLabel lblMensaje;

    public CalendarizacionRecursosController(CalendarizacionRecursosPanel vista, CategoriaService categoriaService, CalendarizacionRecursosService service) {
        this.vista = vista;
        this.service = service;
        this.categoriaService = categoriaService;
        this.fechaSpinner = vista.getFechaSpinner();
        this.categoriaCombo = vista.getCategoriaCombo();
        this.btnCargar = vista.getBtnCargar();
        this.btnImprimir = vista.getBtnImprimir();
        this.lblMensaje = vista.getLblMensaje();
        btnCargar.addActionListener(e -> onCargar());
        btnImprimir.addActionListener(e -> onImprimir());
        cargarCategorias();
        vista.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                if (vista.isShowing()) cargarCategorias();
            }
        });
    }

    private void cargarCategorias() {
        categoriaCombo.removeAllItems();
        for (CategoriaRecurso categoria : categoriaService.listarTodos()) {
            categoriaCombo.addItem(categoria);
        }
    }

    private void onCargar() {
        btnImprimir.setEnabled(false);
        ultimoResultado = null;
        lblMensaje.setText(" ");
        java.util.Date fechaUtil = (java.util.Date) fechaSpinner.getValue();
        LocalDate fecha = fechaUtil.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        CategoriaRecurso categoria = (CategoriaRecurso) categoriaCombo.getSelectedItem();

        if (categoria == null) {
            lblMensaje.setText("Debe seleccionar una categoría.");
            return;
        }

        try {
            ResultadoCalendarizacion resultado = service.construirMatriz(fecha, categoria.getId());
            this.ultimoResultado = resultado;
            this.ultimaFecha = fecha;
            this.ultimaCategoria = categoria;

            if (resultado.estaVacio()) {
                lblMensaje.setText("La categoría seleccionada no tiene recursos registrados.");
                vista.limpiarTabla();
                btnImprimir.setEnabled(false);
                return;
            }

            vista.mostrarResultado(resultado);
            btnImprimir.setEnabled(true);

        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    private void onImprimir() {
        if (ultimoResultado == null || ultimoResultado.estaVacio()) {
            lblMensaje.setText("Primero debe cargar una calendarización con datos.");
            return;
        }

        try {
            List<String> encabezados = new ArrayList<>();
            encabezados.add("Hora");
            for (Recurso recurso : ultimoResultado.getRecursos()) {
                encabezados.add(recurso.getDescripcion());
            }

            ReportTable tabla = new ReportTable(encabezados);
            for (FilaCalendarizacion fila : ultimoResultado.getFilas()) {
                List<String> valores = new ArrayList<>();
                valores.add(fila.getHoraTexto());
                for (Recurso recurso : ultimoResultado.getRecursos()) {
                    valores.add(fila.getValor(recurso.getId()));
                }
                tabla.agregarFila(valores);
            }

            String filtros = "Fecha: " + ultimaFecha + " | Categoría: " + ultimaCategoria.getDescripcion();
            ReportHeader header = new ReportHeader(
                    "Calendarización de Recursos",
                    SessionManager.getInstancia().getUsuarioActual().getId(),
                    filtros
            );

            String nombreSugerido = "calendarizacion_" + ultimaFecha + ".pdf";
            String ruta = vista.elegirRutaGuardado(nombreSugerido);
            if (ruta == null) {
                return; // el usuario canceló el diálogo
            }

            pdfReportService.generarReporteMatriz(ruta, header, tabla);
            lblMensaje.setForeground(new Color(0, 122, 47));
            lblMensaje.setText("Reporte generado en: " + ruta);

        } catch (ReportException e) {
            lblMensaje.setForeground(new Color(180, 0, 0));
            lblMensaje.setText("Error al generar el PDF: " + e.getMessage());
        }
    }

}
