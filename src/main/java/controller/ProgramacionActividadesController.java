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

/** Coordina eventos, consultas remotas y exportación de ProgramacionActividadesPanel. */
public final class ProgramacionActividadesController {
    private static final DateTimeFormatter FORMATO_ENCABEZADO_DIA = DateTimeFormatter.ofPattern("EEE dd/MM");
    private final ProgramacionActividadesPanel vista;
    private final ProgramacionActividadesService service;
    private final PdfReportService pdfReportService = new PdfReportService();

    private ResultadoProgramacionActividades ultimoResultado;
    private LocalDate ultimaFechaReferencia;

    private final JSpinner fechaReferenciaSpinner;
    private final JButton btnCargar;
    private final JButton btnImprimir;
    private final JLabel lblSemana;
    private final JLabel lblMensaje;

    public ProgramacionActividadesController(ProgramacionActividadesPanel vista, ProgramacionActividadesService service) {
        this.vista = vista;
        this.service = service;
        this.fechaReferenciaSpinner = vista.getFechaReferenciaSpinner();
        this.btnCargar = vista.getBtnCargar();
        this.btnImprimir = vista.getBtnImprimir();
        this.lblSemana = vista.getLblSemana();
        this.lblMensaje = vista.getLblMensaje();
        btnCargar.addActionListener(e -> onCargar());
        btnImprimir.addActionListener(e -> onImprimir());
    }

    private void onCargar() {
        btnImprimir.setEnabled(false);
        ultimoResultado = null;
        lblMensaje.setText(" ");
        java.util.Date fechaUtil = (java.util.Date) fechaReferenciaSpinner.getValue();
        LocalDate fechaReferencia = fechaUtil.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        try {
            ResultadoProgramacionActividades resultado = service.construirMatrizSemana(fechaReferencia);
            this.ultimoResultado = resultado;
            this.ultimaFechaReferencia = fechaReferencia;

            lblSemana.setText(DateUtils.etiquetaSemana(fechaReferencia));
            vista.mostrarResultado(resultado);
            btnImprimir.setEnabled(true);

        } catch (IllegalArgumentException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    private void onImprimir() {
        if (ultimoResultado == null) {
            lblMensaje.setText("Primero debe cargar una semana con datos.");
            return;
        }

        try {
            List<String> encabezados = new ArrayList<>();
            encabezados.add("Hora");
            for (LocalDate dia : ultimoResultado.getDias()) {
                encabezados.add(dia.format(FORMATO_ENCABEZADO_DIA));
            }

            ReportTable tabla = new ReportTable(encabezados);
            for (FilaActividad fila : ultimoResultado.getFilas()) {
                List<String> valores = new ArrayList<>();
                valores.add(fila.getHoraTexto());
                for (LocalDate dia : ultimoResultado.getDias()) {
                    valores.add(fila.getValor(dia));
                }
                tabla.agregarFila(valores);
            }

            String filtros = DateUtils.etiquetaSemana(ultimaFechaReferencia);
            ReportHeader header = new ReportHeader(
                    "Programación Semanal de Actividades",
                    SessionManager.getInstancia().getUsuarioActual().getId(),
                    filtros
            );

            String nombreSugerido = "actividades_" + ultimaFechaReferencia + ".pdf";
            String ruta = vista.elegirRutaGuardado(nombreSugerido);
            if (ruta == null) {
                return;
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
