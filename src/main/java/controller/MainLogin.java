package controller;

import api.AuthService;
import view.LoginView;

import javax.swing.*;

/**
 * Clase demo para probar el módulo de login (Juan).
 * No es el punto de entrada oficial del proyecto
 */
public class MainLogin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthService authService = client.Services.get(AuthService.class);

            LoginView loginView = new LoginView();
            LoginController loginController =
                    new LoginController(loginView, authService);

            loginView.setVisible(true);
        });
    }
}