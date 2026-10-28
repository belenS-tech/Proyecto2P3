package controller;

import view.MainView;
import model.Usuario;
import api.CategoriaService;
import api.FuncionarioService;
import api.AuthService;
import view.CambiarClaveView;
import view.LoginView;

public class LoginController {

    private final LoginView vista;
    private final AuthService authService;

    public LoginController(LoginView vista, AuthService authService) {
        this.vista = vista;
        this.authService = authService;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBotonIngresar().addActionListener(e -> intentarIngresar());
        vista.getBotonCancelar().addActionListener(e -> System.exit(0));
        vista.getBotonCambiar().addActionListener(e -> abrirCambiarClave());
    }

    private void intentarIngresar() {
        try {
            String id = vista.getIdIngresado();
            String clave = vista.getClaveIngresada();

            Usuario usuario = authService.iniciarSesion(id, clave);

            service.SessionManager.getInstancia().iniciarSesion(usuario);
            abrirVentanaPrincipal(usuario);
            vista.dispose();

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    private void abrirCambiarClave() {
        String id = vista.getIdIngresado();

        try {
            authService.verificarExiste(id);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return;
        }

        CambiarClaveView vistaClave = new CambiarClaveView(id);
        new CambiarClaveController(vistaClave, authService, id);
        vistaClave.setVisible(true);

        vista.limpiarCampos();
    }

    private void abrirVentanaPrincipal(Usuario usuario) {
        FuncionarioService funcionarioService = client.Services.get(FuncionarioService.class);
        CategoriaService categoriaService = client.Services.get(CategoriaService.class);

        MainView mainView = new MainView(usuario);
        new MainController(mainView, authService, usuario, funcionarioService, categoriaService);
        mainView.setVisible(true);
    }
}