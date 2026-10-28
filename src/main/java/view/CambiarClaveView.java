package view;

import javax.swing.*;
import java.awt.*;

public class CambiarClaveView extends JDialog {

    private JPasswordField campoActual;
    private JPasswordField campoNueva;
    private JPasswordField campoConfirmacion;
    private JButton botonGuardar;
    private JButton botonCancelar;

    /** Muestra el ID en el título para que el usuario sepa de quién es la clave. */
    public CambiarClaveView(String idUsuario) {
        setTitle(idUsuario == null || idUsuario.isBlank()
                ? "Cambiar Clave"
                : "Cambiar Clave - " + idUsuario);
        setModal(true); // bloquea la ventana de atrás hasta cerrarse
        construirVentana();
    }

    public CambiarClaveView() {
        this("");
    }

    private void construirVentana() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(380, 230);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Clave actual:"), gbc);
        campoActual = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        add(campoActual, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Clave nueva:"), gbc);
        campoNueva = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        add(campoNueva, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Confirmar clave:"), gbc);
        campoConfirmacion = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        add(campoConfirmacion, gbc);

        botonGuardar = new JButton("Guardar");
        gbc.gridx = 0; gbc.gridy = 3;
        add(botonGuardar, gbc);

        botonCancelar = new JButton("Cancelar");
        gbc.gridx = 1; gbc.gridy = 3;
        add(botonCancelar, gbc);
    }

    public String getClaveActual() {
        return new String(campoActual.getPassword());
    }

    public String getClaveNueva() {
        return new String(campoNueva.getPassword());
    }

    public String getConfirmacion() {
        return new String(campoConfirmacion.getPassword());
    }

    public void limpiarCampos() {
        campoActual.setText("");
        campoNueva.setText("");
        campoConfirmacion.setText("");
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public JButton getBotonGuardar() {
        return botonGuardar;
    }

    public JButton getBotonCancelar() {
        return botonCancelar;
    }
}