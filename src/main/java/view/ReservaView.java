package view;

import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ReservaView extends JPanel {

    private JTextArea txtFrase;
    private JButton btnExtraerIA;
    private JTextField txtActividad;
    private JSpinner spnFecha;
    private JComboBox<String> cmbHoraInicio;
    private JComboBox<String> cmbHoraFin;
    private JList<CategoriaRecurso> listCategorias;
    private DefaultListModel<CategoriaRecurso> modeloCategorias;
    private JButton btnReservar;
    private JButton btnCancelar;
    private JButton btnLimpiar;
    private JButton btnImprimir;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public ReservaView() {
        construirPantalla();
    }

    private void construirPantalla() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(construirPanelNuevaReserva(), BorderLayout.NORTH);
        add(construirPanelMisReservas(), BorderLayout.CENTER);
        Tema.aplicar(this);
    }

    private JPanel construirPanelNuevaReserva() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nueva reserva"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Frase (label + textarea + botón Extraer)
        txtFrase = new JTextArea(2, 40);
        txtFrase.setLineWrap(true);
        txtFrase.setWrapStyleWord(true);
        JScrollPane scrollFrase = new JScrollPane(txtFrase);

        btnExtraerIA = new JButton("Extraer");
        URL urlIA = getClass().getResource("/icons/ia.png");
        if (urlIA != null) {
            Image img = new ImageIcon(urlIA).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnExtraerIA.setIcon(new ImageIcon(img));
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Frase"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        panel.add(scrollFrase, gbc);
        gbc.gridx = 4; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(btnExtraerIA, gbc);

        // Fila 1: Actividad
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Actividad"), gbc);
        txtActividad = new JTextField();
        gbc.gridx = 1; gbc.gridwidth = 4; gbc.weightx = 1;
        panel.add(txtActividad, gbc);

        // Fila 2: Fecha | ... | Hora inicio | combo
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel("Fecha"), gbc);
        spnFecha = new JSpinner(new SpinnerDateModel());
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd 'de' MMMM 'de' yyyy"));
        gbc.gridx = 1; gbc.weightx = 0.5;
        panel.add(spnFecha, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Hora inicio"), gbc);
        cmbHoraInicio = new JComboBox<>(generarHoras());
        gbc.gridx = 3; gbc.gridwidth = 2; gbc.weightx = 0.5;
        panel.add(cmbHoraInicio, gbc);

        // Fila 3: "Categorías requeridas..." | Hora fin | combo
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel("Categorías requeridas (selección múltiple)"), gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Hora fin"), gbc);
        cmbHoraFin = new JComboBox<>(generarHoras());
        gbc.gridx = 3; gbc.gridwidth = 2; gbc.weightx = 0.5;
        panel.add(cmbHoraFin, gbc);

        // Fila 4: JList categorías (panel con título "Categorías")
        modeloCategorias = new DefaultListModel<>();
        listCategorias = new JList<>(modeloCategorias);
        listCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listCategorias.setBackground(Color.WHITE);
        listCategorias.setSelectionBackground(new Color(70, 130, 180));
        listCategorias.setSelectionForeground(Color.WHITE);
        listCategorias.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int i0, int i1) {
                if (i0 == i1) {
                    if (isSelectedIndex(i0)) super.removeSelectionInterval(i0, i0);
                    else super.addSelectionInterval(i0, i0);
                } else {
                    super.setSelectionInterval(i0, i1);
                }
            }
        });
        JScrollPane scrollCats = new JScrollPane(listCategorias);
        scrollCats.setBorder(BorderFactory.createTitledBorder("Categorías"));
        scrollCats.setPreferredSize(new Dimension(0, 90));

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 5; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollCats, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 5: Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnReservar = new JButton("Reservar");
        btnCancelar = new JButton("Cancelar reserva seleccionada");
        btnLimpiar = new JButton("Limpiar");

        setIcono(btnReservar, "/icons/guardar.png");
        setIcono(btnCancelar, "/icons/eliminar.png");
        setIcono(btnLimpiar, "/icons/limpiar.png");

        botones.add(btnReservar);
        botones.add(btnCancelar);
        botones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 5; gbc.weightx = 1;
        panel.add(botones, gbc);

        return panel;
    }

    private JPanel construirPanelMisReservas() {
        modeloTabla = new DefaultTableModel(
                new String[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Mis reservas"));

        btnImprimir = new JButton("Imprimir");
        setIcono(btnImprimir, "/icons/imprimir.png");

        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelDerecha.add(btnImprimir);

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelDerecha, BorderLayout.EAST);
        return panel;
    }

    private void setIcono(JButton btn, String ruta) {
        URL url = getClass().getResource(ruta);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        }
    }

    private String[] generarHoras() {
        List<String> horas = new ArrayList<>();
        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m += 30)
                horas.add(String.format("%02d:%02d", h, m));
        return horas.toArray(new String[0]);
    }

    public JTextArea getTxtFrase() { return txtFrase; }
    public JButton getBtnExtraerIA() { return btnExtraerIA; }
    public JTextField getTxtActividad() { return txtActividad; }
    public JSpinner getSpnFecha() { return spnFecha; }
    public JComboBox<String> getCmbHoraInicio() { return cmbHoraInicio; }
    public JComboBox<String> getCmbHoraFin() { return cmbHoraFin; }
    public JList<CategoriaRecurso> getListCategorias() { return listCategorias; }
    public DefaultListModel<CategoriaRecurso> getModeloCategorias() { return modeloCategorias; }
    public JButton getBtnReservar() { return btnReservar; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JTable getTabla() { return tabla; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }

    public void mostrarMensaje(String mensaje) { JOptionPane.showMessageDialog(this, mensaje); }
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public String elegirRutaReporte(String nombre) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte PDF");
        selector.setSelectedFile(new java.io.File(nombre));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF", "pdf"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        String ruta = selector.getSelectedFile().getAbsolutePath();
        return ruta.toLowerCase().endsWith(".pdf") ? ruta : ruta + ".pdf";
    }
}
