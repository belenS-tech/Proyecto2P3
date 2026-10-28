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

/** Coordina eventos, consultas remotas y exportación de EstadisticasActividadesPanel. */
public final class EstadisticasActividadesController {
    private final EstadisticasActividadesPanel vista;
    private final EstadisticasActividadesService service;
    private final PdfReportService pdfReportService = new PdfReportService();

    private LinkedHashMap<String, Integer> ultimoResultado;
    private LocalDate ultimoDesde;
    private LocalDate ultimoHasta;

    private final JSpinner fechaDesdeSpinner;
    private final JSpinner fechaHastaSpinner;
    private final JButton btnCalcular;
    private final JButton btnImprimir;
    private final JLabel lblMensaje;

    public EstadisticasActividadesController(EstadisticasActividadesPanel vista, EstadisticasActividadesService service) {
        this.vista = vista;
        this.service = service;
        this.fechaDesdeSpinner = vista.getFechaDesdeSpinner();
        this.fechaHastaSpinner = vista.getFechaHastaSpinner();
        this.btnCalcular = vista.getBtnCalcular();
        this.btnImprimir = vista.getBtnImprimir();
        this.lblMensaje = vista.getLblMensaje();
        btnCalcular.addActionListener(e -> onCalcular());
        btnImprimir.addActionListener(e -> onImprimir());
    }

    private void onCalcular() {
        btnImprimir.setEnabled(false);
        ultimoResultado = null;
        if (service == null) {
            lblMensaje.setText("El servicio no está configurado.");
            return;
        }
        lblMensaje.setText(" ");
        LocalDate desde = obtenerFecha(fechaDesdeSpinner);
        LocalDate hasta = obtenerFecha(fechaHastaSpinner);

        try {
            LinkedHashMap<String, Integer> resultado = service.calcular(desde, hasta);
            this.ultimoResultado = resultado;
            this.ultimoDesde = desde;
            this.ultimoHasta = hasta;

            vista.mostrarResultado(resultado);

            if (resultado.isEmpty()) {
                lblMensaje.setText("No se encontraron actividades en el período seleccionado.");
            }
            btnImprimir.setEnabled(true);

        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
            btnImprimir.setEnabled(false);
        }
    }

    private LocalDate obtenerFecha(JSpinner spinner) {
        java.util.Date fechaUtil = (java.util.Date) spinner.getValue();
        return fechaUtil.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void onImprimir() {
        if (ultimoResultado == null) {
            lblMensaje.setText("Primero debe calcular las estadísticas.");
            return;
        }

        try {
            ReportTable tabla = new ReportTable(List.of("Semana", "Cantidad de actividades"));
            for (Map.Entry<String, Integer> entry : ultimoResultado.entrySet()) {
                tabla.agregarFila(entry.getKey(), String.valueOf(entry.getValue()));
            }

            String filtros = "Desde: " + ultimoDesde + " | Hasta: " + ultimoHasta;
            ReportHeader header = new ReportHeader(
                    "Estadísticas de Actividades",
                    SessionManager.getInstancia().getUsuarioActual().getId(),
                    filtros
            );

            byte[] imagenGrafico = vista.capturarGraficoComoPng();

            String nombreSugerido = "estadisticas_actividades_" + ultimoDesde + "_" + ultimoHasta + ".pdf";
            String ruta = vista.elegirRutaGuardado(nombreSugerido);
            if (ruta == null) {
                return;
            }

            pdfReportService.reporteGrafico(ruta, header, tabla, imagenGrafico);
            lblMensaje.setForeground(new Color(0, 122, 47));
            lblMensaje.setText("Reporte generado en: " + ruta);

        } catch (Exception e) {
            lblMensaje.setForeground(new Color(223, 95, 243));
            lblMensaje.setText("Error al generar el PDF: " + e.getMessage());
        }
    }

}
