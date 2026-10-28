import javax.swing.*;
import controller.LoginController;
import view.LoginView;
import api.AuthService;
public class Main {
    public static void main(String[] args) {
        try { client.Services.connection(); }
        catch (IllegalArgumentException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.getMessage(), "Conexión al servidor", JOptionPane.ERROR_MESSAGE));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            AuthService authService = client.Services.get(AuthService.class);
            LoginView loginView = new LoginView();
            new LoginController(loginView, authService);
            loginView.setVisible(true);
        });
    }
}
