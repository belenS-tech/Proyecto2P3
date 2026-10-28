package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {

    private static final Color COLOR_FONDO       = new Color(30, 30, 45);
    private static final Color COLOR_PANEL        = new Color(45, 45, 65);
    private static final Color COLOR_ACENTO       = new Color(99, 102, 241);
    private static final Color COLOR_ACENTO_HOVER = new Color(79, 82, 221);
    private static final Color COLOR_TEXTO        = new Color(230, 230, 255);
    private static final Color COLOR_CAMPO        = new Color(60, 60, 85);
    private static final Color COLOR_BORDE_CAMPO  = new Color(99, 102, 241);

    private JTextField campoId;
    private JPasswordField campoClave;
    private JButton botonIngresar;
    private JButton botonCancelar;
    private JButton botonCambiar;

    public LoginView() {
        super("Sistema de Reservas");
        construirVentana();
    }

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_PANEL);
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACENTO, 1, true),
                new EmptyBorder(30, 35, 30, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("-- Sistema de Reservas --", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(COLOR_ACENTO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 6, 20, 6);
        card.add(titulo, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);

        JLabel lblId = new JLabel("ID:");
        lblId.setForeground(COLOR_TEXTO);
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = 1;
        card.add(lblId, gbc);

        campoId = estilizarCampo(new JTextField(15));
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        card.add(campoId, gbc);
        gbc.gridwidth = 1;

        JLabel lblClave = new JLabel("Clave:");
        lblClave.setForeground(COLOR_TEXTO);
        lblClave.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0; gbc.gridy = 2;
        card.add(lblClave, gbc);

        campoClave = (JPasswordField) estilizarCampo(new JPasswordField(15));
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        card.add(campoClave, gbc);
        gbc.gridwidth = 1;

        gbc.insets = new Insets(18, 6, 6, 6);
        botonIngresar = crearBoton("Ingresar", COLOR_ACENTO, COLOR_ACENTO_HOVER);
        gbc.gridx = 0; gbc.gridy = 3;
        card.add(botonIngresar, gbc);

        botonCancelar = crearBoton("Cancelar", new Color(80, 80, 100), new Color(100, 100, 120));
        gbc.gridx = 1; gbc.gridy = 3;
        card.add(botonCancelar, gbc);

        botonCambiar = crearBoton("Cambiar clave", new Color(80, 80, 100), new Color(100, 100, 120));
        gbc.gridx = 2; gbc.gridy = 3;
        card.add(botonCambiar, gbc);

        add(card);
    }

    private <T extends JTextField> T estilizarCampo(T campo) {
        campo.setBackground(COLOR_CAMPO);
        campo.setForeground(COLOR_TEXTO);
        campo.setCaretColor(COLOR_TEXTO);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE_CAMPO, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return campo;
    }

    private JButton crearBoton(String texto, Color fondo, Color hover) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 34));
        return btn;
    }

    public String getIdIngresado() { return campoId.getText(); }
    public String getClaveIngresada() { return new String(campoClave.getPassword()); }
    public void limpiarCampos() { campoId.setText(""); campoClave.setText(""); }
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public JButton getBotonIngresar() { return botonIngresar; }
    public JButton getBotonCancelar() { return botonCancelar; }
    public JButton getBotonCambiar() { return botonCambiar; }
}
