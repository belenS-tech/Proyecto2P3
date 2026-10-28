package service;

import model.Rol;
import model.Usuario;

public class SessionManager {

    private static final SessionManager instancia = new SessionManager();
    private final ThreadLocal<Usuario> usuarioActual = new ThreadLocal<>();

    private SessionManager() {
    }

    public static SessionManager getInstancia() {
        return instancia;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual.get();
    }

    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual.set(usuario);
    }

    public void cerrarSesion() {
        this.usuarioActual.remove();
    }

    public boolean haySesionActiva() {
        return usuarioActual.get() != null;
    }

    public boolean esAdministrador() {
        return haySesionActiva() && usuarioActual.get().getRol() == Rol.ADMINISTRADOR;
    }
}
