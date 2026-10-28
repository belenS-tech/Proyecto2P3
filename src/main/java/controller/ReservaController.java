package controller;

import model.*;
import api.*;
import service.SessionManager;
import service.AiReservationResponse;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ReservaController {

    private final view.ReservaView vista;

    private final ReservaService reservaService;
    private final CategoriaService categoriaService;
    private final AiReservationService aiReservationService;

    private final JTextArea txtFrase;
    private final JButton btnExtraerIA;
    private final JTextField txtActividad;
    private final JSpinner spnFecha;
    private final JComboBox<String> cmbHoraInicio;
    private final JComboBox<String> cmbHoraFin;
    private final JList<CategoriaRecurso> listCategorias;
    private final DefaultListModel<CategoriaRecurso> modeloCategorias;
    private final JButton btnReservar;
    private final JButton btnCancelar;
    private final JButton btnLimpiar;
    private final JButton btnImprimir;
    private final JTable tabla;
    private final DefaultTableModel modeloTabla;

    public ReservaController(view.ReservaView vista, CategoriaService categoriaService) {
        this(vista, categoriaService, client.Services.get(ReservaService.class), client.Services.get(AiReservationService.class));
    }

    public ReservaController(view.ReservaView vista, CategoriaService categoriaService,
                             ReservaService reservaService, AiReservationService aiReservationService) {
        this.vista = vista;
        this.txtFrase = vista.getTxtFrase();
        this.btnExtraerIA = vista.getBtnExtraerIA();
        this.txtActividad = vista.getTxtActividad();
        this.spnFecha = vista.getSpnFecha();
        this.cmbHoraInicio = vista.getCmbHoraInicio();
        this.cmbHoraFin = vista.getCmbHoraFin();
        this.listCategorias = vista.getListCategorias();
        this.modeloCategorias = vista.getModeloCategorias();
        this.btnReservar = vista.getBtnReservar();
        this.btnCancelar = vista.getBtnCancelar();
        this.btnLimpiar = vista.getBtnLimpiar();
        this.btnImprimir = vista.getBtnImprimir();
        this.tabla = vista.getTabla();
        this.modeloTabla = vista.getModeloTabla();

        this.categoriaService = categoriaService;
        this.reservaService = reservaService;
        this.aiReservationService = aiReservationService;

        cargarCategorias();
        cargarTabla();
        registrarEventos();

        vista.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (!vista.isShowing()) return;
                cargarCategorias();
                cargarTabla();
            }
        });
    }

    private void cargarCategorias() {
        modeloCategorias.clear();
        for (CategoriaRecurso cat : categoriaService.listarTodos())
            modeloCategorias.addElement(cat);
        listCategorias.revalidate();
        listCategorias.repaint();
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        String usuarioActual = SessionManager.getInstancia().getUsuarioActual() != null
                ? SessionManager.getInstancia().getUsuarioActual().getId() : null;
        for (Reserva reserva : reservaService.listarTodos()) {
            if (usuarioActual != null && !reserva.getFuncionarioId().equals(usuarioActual)) continue;
            String horario = reserva.getHoraInicio() + " - " + reserva.getHoraFin();
            String recursos = reserva.getDetalles().stream()
                    .filter(d -> d.getRecurso() != null)
                    .map(d -> d.getRecurso().getId())
                    .reduce((a, b) -> a + ", " + b).orElse("");
            modeloTabla.addRow(new Object[]{
                    reserva.getId(), reserva.getActividad(), reserva.getFecha(),
                    horario, recursos, reserva.getEstado()
            });
        }
    }

    private void registrarEventos() {
        btnReservar.addActionListener(e -> agregarReserva());
        btnCancelar.addActionListener(e -> cancelarReserva());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnExtraerIA.addActionListener(e -> extraerConIA());
        btnImprimir.addActionListener(e -> imprimir());
        tabla.getSelectionModel().addListSelectionListener(this::alSeleccionarFila);
    }

    private void extraerConIA() {
        String frase = txtFrase.getText();
        btnExtraerIA.setEnabled(false);
        new SwingWorker<AiReservationResponse, Void>() {
            @Override protected AiReservationResponse doInBackground() throws Exception {
                return aiReservationService.interpretarSolicitud(frase);
            }
            @Override protected void done() {
                btnExtraerIA.setEnabled(true);
                try {
                    AiReservationResponse r = get();
                    txtActividad.setText(r.getActividad());
                    spnFecha.setValue(Date.from(r.getFecha().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    seleccionarHora(cmbHoraInicio, r.getHoraInicio());
                    seleccionarHora(cmbHoraFin, r.getHoraFin());
                    seleccionarCategoriasSugeridas(r.getCategorias());
                    mostrarMensaje("Datos extraídos. Revíselos y presione \"Reservar\" para confirmar.");
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    mostrarError("No fue posible interpretar la solicitud: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private void seleccionarHora(JComboBox<String> cmb, LocalTime hora) {
        String texto = hora.toString();
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).equals(texto)) { cmb.setSelectedIndex(i); return; }
            if (hora.isBefore(LocalTime.parse(cmb.getItemAt(i)))) {
                cmb.insertItemAt(texto, i);
                cmb.setSelectedIndex(i);
                return;
            }
        }
        cmb.addItem(texto);
        cmb.setSelectedItem(texto);
    }

    private void seleccionarCategoriasSugeridas(List<CategoriaRecurso> sugeridas) {
        DefaultListSelectionModel modeloBase = (DefaultListSelectionModel) listCategorias.getSelectionModel();
        modeloBase.clearSelection();
        if (sugeridas == null) return;
        for (int i = 0; i < modeloCategorias.size(); i++) {
            CategoriaRecurso cat = modeloCategorias.get(i);
            boolean coincide = sugeridas.stream().anyMatch(s ->
                    (s.getDescripcion() != null && s.getDescripcion().equalsIgnoreCase(cat.getDescripcion()))
                    || (s.getId() != null && s.getId().equalsIgnoreCase(cat.getDescripcion()))
                    || (s.getId() != null && s.getId().equalsIgnoreCase(cat.getId())));
            if (coincide) modeloBase.addSelectionInterval(i, i);
        }
    }

    private List<CategoriaRecurso> getCategoriasSeleccionadas() {
        return listCategorias.getSelectedValuesList();
    }

    private void agregarReserva() {
        List<CategoriaRecurso> seleccionadas = getCategoriasSeleccionadas();
        if (seleccionadas.isEmpty()) { mostrarError("Debe seleccionar al menos una categoría."); return; }
        try {
            Reserva reserva = construirReservaBase();
            // El cliente solicita categorías; el backend asigna recursos al guardar.
            reserva.setDetalles(seleccionadas.stream()
                    .map(cat -> new DetalleReserva(cat, null)).toList());
            reservaService.registrar(reserva);
            cargarTabla();
            limpiarCampos();
            mostrarMensaje("Reserva registrada correctamente.");
        } catch (RuntimeException ex) { mostrarError(ex.getMessage()); }
    }

    private void cancelarReserva() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarError("Seleccione una reserva de la tabla."); return; }
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));
        try {
            Reserva reserva = reservaService.buscarPorId(id);
            if (reserva == null) { mostrarError("No se encontró la reserva."); return; }
            reservaService.eliminar(reserva);
            cargarTabla();
            limpiarCampos();
            mostrarMensaje("Reserva cancelada correctamente.");
        } catch (RuntimeException ex) { mostrarError(ex.getMessage()); }
    }

    private void alSeleccionarFila(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        txtActividad.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        Object fechaObj = modeloTabla.getValueAt(fila, 2);
        if (fechaObj instanceof LocalDate fecha)
            spnFecha.setValue(Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        String horario = String.valueOf(modeloTabla.getValueAt(fila, 3));
        String[] partes = horario.split(" - ");
        if (partes.length == 2) {
            seleccionarHora(cmbHoraInicio, LocalTime.parse(partes[0]));
            seleccionarHora(cmbHoraFin, LocalTime.parse(partes[1]));
        }
    }

    private Reserva construirReservaBase() {
        String usuarioId = SessionManager.getInstancia().getUsuarioActual() != null
                ? SessionManager.getInstancia().getUsuarioActual().getId() : "";
        LocalDate fecha = ((Date) spnFecha.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime inicio = LocalTime.parse((String) cmbHoraInicio.getSelectedItem());
        LocalTime fin = LocalTime.parse((String) cmbHoraFin.getSelectedItem());
        // El backend asigna el identificador al registrar, bajo el bloqueo de escritura.
        return new Reserva("", txtActividad.getText(), fecha, inicio, fin, usuarioId, EstadoReserva.ACTIVA);
    }

    private void limpiarCampos() {
        txtFrase.setText("");
        txtActividad.setText("");
        spnFecha.setValue(new Date());
        cmbHoraInicio.setSelectedIndex(0);
        cmbHoraFin.setSelectedIndex(0);
        listCategorias.clearSelection();
        tabla.clearSelection();
    }

    private void imprimir() {
        try {
            report.ReportTable reportTabla = new report.ReportTable(
                    java.util.List.of("Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"));
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                reportTabla.agregarFila(
                        String.valueOf(modeloTabla.getValueAt(i, 0)),
                        String.valueOf(modeloTabla.getValueAt(i, 1)),
                        String.valueOf(modeloTabla.getValueAt(i, 2)),
                        String.valueOf(modeloTabla.getValueAt(i, 3)),
                        String.valueOf(modeloTabla.getValueAt(i, 4)),
                        String.valueOf(modeloTabla.getValueAt(i, 5)));
            }
            String usuario = SessionManager.getInstancia().getUsuarioActual() != null
                    ? SessionManager.getInstancia().getUsuarioActual().getId() : "";
            report.ReportHeader header = new report.ReportHeader("Mis Reservas", usuario, null);
            String ruta = vista.elegirRutaReporte("mis_reservas.pdf");
            if (ruta == null) return;

            new report.PdfReportService().generarReporteTabla(ruta, header, reportTabla);
            mostrarMensaje("Reporte generado: " + ruta);
        } catch (Exception ex) {
            mostrarError("Error al generar el reporte: " + ex.getMessage());
        }
    }

    private void mostrarMensaje(String m) { vista.mostrarMensaje(m); }
    private void mostrarError(String m) { vista.mostrarError(m); }
}
