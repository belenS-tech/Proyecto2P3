package view;

import model.CategoriaRecurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.List;
//categorias
public class CategoriasView extends JPanel {

    private JTextField campoBuscarDescripcion;
    private JButton botonBuscar;
    private JButton botonImprimir;

    private JTextField campoId;
    private JTextField campoDescripcion;
    private JButton botonGuardar;
    private JButton botonBorrar;
    private JButton botonLimpiar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public CategoriasView() {
        construirPantalla();
    }

    private void construirPantalla() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.add(construirPanelBusqueda(), BorderLayout.NORTH);
        panelSuperior.add(construirPanelFormulario(), BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(construirPanelListado(), BorderLayout.CENTER);
        Tema.aplicar(this);
    }

    private JPanel construirPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Búsqueda"));

        panel.add(new JLabel("Descripción:"));
        campoBuscarDescripcion = new JTextField(20);
        panel.add(campoBuscarDescripcion);

        botonBuscar = new JButton("Buscar");
        URL urlBuscar = getClass().getResource("/icons/buscar.png");
        if (urlBuscar != null) {
            ImageIcon original = new ImageIcon(urlBuscar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            botonBuscar.setIcon(new ImageIcon(img));
        }
        panel.add(botonBuscar);

        botonImprimir = new JButton("Imprimir");
        URL urlImprimir = getClass().getResource("/icons/imprimir.png");
        if (urlImprimir != null) {
            ImageIcon original = new ImageIcon(urlImprimir);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            botonImprimir.setIcon(new ImageIcon(img));
        }
        panel.add(botonImprimir);

        return panel;
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Categoría"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID (automático):"), gbc);
        campoId = new JTextField(22);
        campoId.setEditable(false);
        campoId.setToolTipText("El ID se genera automáticamente al guardar");
        campoId.setText("(se asigna al guardar)");
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(campoId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Descripción:"), gbc);
        campoDescripcion = new JTextField(22);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(campoDescripcion, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        botonGuardar = new JButton("Guardar");
        botonBorrar = new JButton("Borrar");
        botonLimpiar = new JButton("Limpiar");

        URL urlGuardar = getClass().getResource("/icons/guardar.png");
        if (urlGuardar != null) {
            ImageIcon original = new ImageIcon(urlGuardar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            botonGuardar.setIcon(new ImageIcon(img));
        }

        URL urlBorrar = getClass().getResource("/icons/eliminar.png");
        if (urlBorrar != null) {
            ImageIcon original = new ImageIcon(urlBorrar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            botonBorrar.setIcon(new ImageIcon(img));
        }

        URL urlLimpiar = getClass().getResource("/icons/limpiar.png");
        if (urlLimpiar != null) {
            ImageIcon original = new ImageIcon(urlLimpiar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            botonLimpiar.setIcon(new ImageIcon(img));
        }

        panelBotones.add(botonGuardar);
        panelBotones.add(botonBorrar);
        panelBotones.add(botonLimpiar);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JScrollPane construirPanelListado() {
        modeloTabla = new DefaultTableModel(new String[]{"Id", "Descripcion"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado"));
        return scroll;
    }

    public void cargarTabla(List<CategoriaRecurso> categorias) {
        modeloTabla.setRowCount(0);
        for (CategoriaRecurso c : categorias) {
            modeloTabla.addRow(new Object[]{c.getId(), c.getDescripcion()});
        }
    }

    public String getIdSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return (String) modeloTabla.getValueAt(fila, 0);
    }

    public void mostrarEnFormulario(CategoriaRecurso categoria) {
        campoId.setText(categoria.getId());
        campoDescripcion.setText(categoria.getDescripcion());
    }

    public void limpiarFormulario() {
        campoId.setText("(se asigna al guardar)");
        campoDescripcion.setText("");
        tabla.clearSelection();
        campoDescripcion.requestFocus();
    }

    public String getId() {
        String texto = campoId.getText();
        return texto.equals("(se asigna al guardar)") ? "" : texto;
    }

    public String getDescripcion() {
        return campoDescripcion.getText();
    }

    public String getBuscarDescripcion() {
        return campoBuscarDescripcion.getText();
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    public boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public JButton getBotonBuscar() {
        return botonBuscar;
    }

    public JButton getBotonImprimir() {
        return botonImprimir;
    }

    public JButton getBotonGuardar() {
        return botonGuardar;
    }

    public JButton getBotonBorrar() {
        return botonBorrar;
    }

    public JButton getBotonLimpiar() {
        return botonLimpiar;
    }

    public JTable getTabla() {
        return tabla;
    }
    public String elegirRutaReporte(String nombre) {
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
        selector.setDialogTitle("Guardar reporte PDF");
        selector.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));
        selector.setSelectedFile(new java.io.File(nombre));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));
        if (selector.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return null;
        String ruta = selector.getSelectedFile().getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".pdf")) ruta += ".pdf";
        return ruta;
    }
}
