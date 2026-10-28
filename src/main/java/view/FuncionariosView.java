package view;

import model.Funcionario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class FuncionariosView extends JPanel {

    private JTextField campoBuscarId;
    private JTextField campoBuscarNombre;
    private JButton botonBuscar;
    private JButton botonImprimir;

    private JTextField campoId;
    private JTextField campoNombre;
    private JTextField campoTelefono;
    private JButton botonGuardar;
    private JButton botonBorrar;
    private JButton botonLimpiar;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public FuncionariosView() {
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

        panel.add(new JLabel("ID:"));
        campoBuscarId = new JTextField(10);
        panel.add(campoBuscarId);

        panel.add(new JLabel("Nombre:"));
        campoBuscarNombre = new JTextField(15);
        panel.add(campoBuscarNombre);

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
        panel.setBorder(BorderFactory.createTitledBorder("Funcionario"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        campoId = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(campoId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre:"), gbc);
        campoNombre = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(campoNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Teléfono:"), gbc);
        campoTelefono = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(campoTelefono, gbc);

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

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JScrollPane construirPanelListado() {
        modeloTabla = new DefaultTableModel(new String[]{"Id", "Nombre", "Teléfono"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false; // la tabla es solo de consulta
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado"));
        return scroll;
    }

    public void cargarTabla(List<Funcionario> funcionarios) {
        modeloTabla.setRowCount(0);
        for (Funcionario f : funcionarios) {
            modeloTabla.addRow(new Object[]{f.getId(), f.getNombre(), f.getTelefono()});
        }
    }

    /** Devuelve el ID del funcionario seleccionado, o null si no hay selección. */
    public String getIdSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return (String) modeloTabla.getValueAt(fila, 0);
    }

    public void mostrarEnFormulario(Funcionario funcionario) {
        campoId.setText(funcionario.getId());
        campoNombre.setText(funcionario.getNombre());
        campoTelefono.setText(funcionario.getTelefono());
    }

    public void limpiarFormulario() {
        campoId.setText("");
        campoNombre.setText("");
        campoTelefono.setText("");
        tabla.clearSelection();
    }

    public String getId() {
        return campoId.getText();
    }

    public String getNombre() {
        return campoNombre.getText();
    }

    public String getTelefono() {
        return campoTelefono.getText();
    }

    public String getBuscarId() {
        return campoBuscarId.getText();
    }

    public String getBuscarNombre() {
        return campoBuscarNombre.getText();
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
