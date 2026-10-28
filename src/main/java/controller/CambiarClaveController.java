package controller;

import api.AuthService;
import view.CambiarClaveView;

public class CambiarClaveController {

    private final CambiarClaveView vista;
    private final AuthService authService;
    private final String idUsuario;

    /** Desde el login: se recibe el ID escrito en la pantalla anterior. */
    public CambiarClaveController(CambiarClaveView vista, AuthService authService, String idUsuario) {
        this.vista = vista;
        this.authService = authService;
        this.idUsuario = idUsuario;
        registrarEventos();
    }

    /** Desde el sistema: se usa el usuario de la sesión activa. */
    public CambiarClaveController(CambiarClaveView vista, AuthService authService) {
        this(vista, authService, null);
    }

    private void registrarEventos() {
        vista.getBotonGuardar().addActionListener(e -> guardar());
        vista.getBotonCancelar().addActionListener(e -> vista.dispose());
    }

    private void guardar() {
        try {
            if (idUsuario == null) {
                authService.cambiarClave(
                        vista.getClaveActual(),
                        vista.getClaveNueva(),
                        vista.getConfirmacion());
            } else {
                authService.cambiarClave(
                        idUsuario,
                        vista.getClaveActual(),
                        vista.getClaveNueva(),
                        vista.getConfirmacion());
            }

            vista.mostrarExito("La clave se cambió correctamente.");
            vista.dispose();

        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }
}