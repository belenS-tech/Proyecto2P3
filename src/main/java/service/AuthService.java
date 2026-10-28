package service;

import model.Usuario;
import repository.UsuarioRepository;

public class AuthService implements api.AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario iniciarSesion(String id, String clave) {
        if (id == null || id.isBlank() || clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("El ID y la clave no pueden estar vacíos.");
        }

        Usuario usuario = usuarioRepository.buscarPorId(id);
        if (usuario == null) {
            throw new IllegalArgumentException("No existe un usuario con ese ID.");
        }

        if (!usuario.getClave().equals(clave)) {
            throw new IllegalArgumentException("La clave es incorrecta.");
        }


        SessionManager.getInstancia().iniciarSesion(usuario);
        return usuario;
    }

    /** Verifica que el ID corresponda a un usuario registrado. */
    public void verificarExiste(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el ID del usuario.");
        }
        if (usuarioRepository.buscarPorId(id) == null) {
            throw new IllegalArgumentException("No existe un usuario con ese ID.");
        }
    }

    /**
     * Cambio de clave desde el login: el ID viene de la pantalla anterior,
     * por lo que no requiere una sesión activa.
     */
    public void cambiarClave(String id, String claveActual, String claveNueva, String confirmacion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el ID del usuario.");
        }

        Usuario usuario = usuarioRepository.buscarPorId(id);
        if (usuario == null) {
            throw new IllegalArgumentException("No existe un usuario con ese ID.");
        }

        aplicarCambio(usuario, claveActual, claveNueva, confirmacion);
    }

    /**
     * Cambio de clave desde el sistema: usa el usuario de la sesión activa.
     */
    public void cambiarClave(String claveActual, String claveNueva, String confirmacion) {
        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            throw new IllegalStateException("No hay una sesión activa.");
        }

        aplicarCambio(usuario, claveActual, claveNueva, confirmacion);
    }

    private void aplicarCambio(Usuario usuario, String claveActual, String claveNueva, String confirmacion) {
        if (!usuario.getClave().equals(claveActual)) {
            throw new IllegalArgumentException("La clave actual no coincide.");
        }

        if (claveNueva == null || claveNueva.isBlank()) {
            throw new IllegalArgumentException("La clave nueva no puede estar vacía.");
        }

        if (!claveNueva.equals(confirmacion)) {
            throw new IllegalArgumentException("Las claves nuevas no coinciden.");
        }

        usuario.setClave(claveNueva);
        usuarioRepository.guardar(usuario);
    }
}
