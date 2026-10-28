package view;

import model.CategoriaRecurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;


public class RecursoView extends JPanel {

    // Filtro
    private JComboBox<CategoriaRecurso> cmbFiltroCategoria;
    private JTextField txtFiltroDescripcion;
    private JButton btnBuscar;
    private JButton btnImprimir;

    // Formulario
    private JTextField txtId;
    private JComboBox<CategoriaRecurso> cmbCategoria;
    private JTextField txtDescripcion;
    private JButton btnAgregar;
    private JButton btnModificar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnConsultar;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public RecursoView() {
        construirPantalla();
        Tema.aplicar(this);
    }

    private void construirPantalla() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.add(construirPanelFiltro(), BorderLayout.NORTH);
        panelSuperior.add(construirFormulario(), BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
    }

    private JPanel construirPanelFiltro() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Filtro"));

        panel.add(new JLabel("Categoría:"));
        cmbFiltroCategoria = new JComboBox<>();
        cmbFiltroCategoria.setPreferredSize(new Dimension(160, 26));
        panel.add(cmbFiltroCategoria);

        panel.add(new JLabel("Descripción:"));
        txtFiltroDescripcion = new JTextField(16);
        panel.add(txtFiltroDescripcion);

        btnBuscar = new JButton("Buscar");
        btnImprimir = new JButton("Imprimir");
        panel.add(btnBuscar);
        panel.add(btnImprimir);

        return panel;
    }

    private JPanel construirFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recurso"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        txtId = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Categoría:"), gbc);
        cmbCategoria = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(cmbCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Descripción:"), gbc);
        txtDescripcion = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(txtDescripcion, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnAgregar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");
        btnConsultar = new JButton("Consultar");
        URL urlBorrar = getClass().getResource("/icons/eliminar.png");
        if (urlBorrar != null) {
            ImageIcon original = new ImageIcon(urlBorrar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnEliminar.setIcon(new ImageIcon(img));
        }

        URL urlLimpiar = getClass().getResource("/icons/limpiar.png");
        if (urlLimpiar != null) {
            ImageIcon original = new ImageIcon(urlLimpiar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnLimpiar.setIcon(new ImageIcon(img));
        }

        URL urlAgregar = getClass().getResource("/icons/agregar.png");
        if (urlAgregar != null) {
            ImageIcon original = new ImageIcon(urlAgregar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnAgregar.setIcon(new ImageIcon(img));
        }

        URL urlConsultar = getClass().getResource("/icons/consultar.png");
        if (urlConsultar != null) {
            ImageIcon original = new ImageIcon(urlConsultar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnConsultar.setIcon(new ImageIcon(img));
        }

        URL urlModificar = getClass().getResource("/icons/modificar.png");
        if (urlModificar != null) {
            ImageIcon original = new ImageIcon(urlModificar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnModificar.setIcon(new ImageIcon(img));
        }

        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(botones, gbc);

        return panel;
    }

    private JScrollPane construirTabla() {
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Categoría", "Descripción"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado"));
        return scroll;
    }

    public JComboBox<CategoriaRecurso> getCmbFiltroCategoria() { return cmbFiltroCategoria; }
    public JTextField getTxtFiltroDescripcion() { return txtFiltroDescripcion; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JTextField getTxtId() { return txtId; }
    public JComboBox<CategoriaRecurso> getCmbCategoria() { return cmbCategoria; }
    public JTextField getTxtDescripcion() { return txtDescripcion; }
    public JButton getBtnAgregar() { return btnAgregar; }
    public JButton getBtnModificar() { return btnModificar; }
    public JButton getBtnEliminar() { return btnEliminar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnConsultar() { return btnConsultar; }
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
