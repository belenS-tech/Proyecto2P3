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

/** Coordina eventos, consultas remotas y exportación de EstadisticasRecursosPanel. */
public final class EstadisticasRecursosController {
    private final EstadisticasRecursosPanel vista;
    private final EstadisticasRecursosService servicio;
    private final PdfReportService pdfReportService = new PdfReportService();

    private Map<String, Integer> ultimoResultado;
    private String ultimoPeriodo;

    private final JSpinner spnDesde;
    private final JSpinner spnHasta;
    private final JButton btnCalcular;
    private final JButton btnImprimir;
    private final JLabel lblMensaje;

    public EstadisticasRecursosController(EstadisticasRecursosPanel vista, EstadisticasRecursosService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        this.spnDesde = vista.getSpnDesde();
        this.spnHasta = vista.getSpnHasta();
        this.btnCalcular = vista.getBtnCalcular();
        this.btnImprimir = vista.getBtnImprimir();
        this.lblMensaje = vista.getLblMensaje();
        btnCalcular.addActionListener(e -> onCalcular());
        btnImprimir.addActionListener(e -> onImprimir());
    }

    private void onCalcular() {
        btnImprimir.setEnabled(false);
        ultimoResultado = null;
        if (servicio == null) {
            mostrarError("El servicio no está configurado.");
            return;
        }
        try {
            LocalDate desde = obtenerFecha(spnDesde);
            LocalDate hasta = obtenerFecha(spnHasta);

            Map<String, Integer> resultado = servicio.contarPorCategoria(desde, hasta);

            ultimoResultado = resultado;
            ultimoPeriodo = "Del " + desde + " al " + hasta;

            vista.mostrarResultado(resultado);

            if (resultado.isEmpty()) {
                mostrarError("No hay reservas en el período indicado.");
            } else {
                lblMensaje.setText(" ");
            }
            btnImprimir.setEnabled(true);

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private LocalDate obtenerFecha(JSpinner spinner) {
        java.util.Date fechaUtil = (java.util.Date) spinner.getValue();
        return fechaUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void onImprimir() {
        if (ultimoResultado == null) {
            mostrarError("Primero debe calcular las estadísticas.");
            return;
        }
        try {
            ReportTable tabla = new ReportTable(List.of("Categoría", "Cantidad"));
            for (Map.Entry<String, Integer> entrada : ultimoResultado.entrySet())
                tabla.agregarFila(entrada.getKey(), String.valueOf(entrada.getValue()));

            ReportHeader header = new ReportHeader(
                    "Estadísticas de Recursos",
                    SessionManager.getInstancia().getUsuarioActual().getId(),
                    ultimoPeriodo);

            byte[] imagenGrafico = vista.capturarGraficoComoPng();
            String ruta = vista.elegirRutaGuardado("estadisticas_recursos.pdf");
            if (ruta == null) return;

            pdfReportService.reporteGrafico(ruta, header, tabla, imagenGrafico);
            lblMensaje.setForeground(new Color(0, 122, 47));
            lblMensaje.setText("Reporte generado en: " + ruta);

        } catch (ReportException e) {
            mostrarError("Error al generar el PDF: " + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) { vista.mostrarError(mensaje); }
}
