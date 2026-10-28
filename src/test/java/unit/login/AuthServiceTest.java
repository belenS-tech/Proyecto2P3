package unit.login;

import model.Rol;
import model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.SessionManager;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private UsuarioRepositoryEnMemoria usuarios;

    @BeforeEach
    void prepararCadaPrueba() {
        usuarios = new UsuarioRepositoryEnMemoria();
        usuarios.guardar(new Usuario("admin", "admin", Rol.ADMINISTRADOR));
        usuarios.guardar(new Usuario("111", "111", Rol.FUNCIONARIO));

        authService = new AuthService(usuarios);
        SessionManager.getInstancia().cerrarSesion();
    }

    @AfterEach
    void limpiarSesion() {
        SessionManager.getInstancia().cerrarSesion();
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = authService.iniciarSesion("admin", "admin");

        assertNotNull(usuario);
        assertEquals("admin", usuario.getId());
        assertEquals(Rol.ADMINISTRADOR, usuario.getRol());
    }

    @Test
    void laSesionQuedaActivaDespuesDeIngresar() {
        authService.iniciarSesion("111", "111");

        assertTrue(SessionManager.getInstancia().haySesionActiva());
        assertEquals("111", SessionManager.getInstancia().getUsuarioActual().getId());
    }

    @Test
    void rechazaClaveIncorrecta() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion("admin", "otraclave"));
    }

    @Test
    void rechazaUsuarioInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion("pedro", "1234"));
    }

    @Test
    void rechazaCamposVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion("", ""));
        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion("admin", ""));
        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion(null, null));
    }

    @Test
    void elIdDistingueMayusculas() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.iniciarSesion("ADMIN", "admin"));
    }

    @Test
    void verificarExisteAceptaUnIdRegistrado() {
        assertDoesNotThrow(() -> authService.verificarExiste("admin"));
    }

    @Test
    void verificarExisteRechazaUnIdDesconocido() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.verificarExiste("pedro"));
        assertThrows(IllegalArgumentException.class,
                () -> authService.verificarExiste(""));
    }

    @Test
    void cambiaLaClaveDesdeElLogin() {
        authService.cambiarClave("111", "111", "nueva", "nueva");

        assertEquals("nueva", usuarios.buscarPorId("111").getClave());
        assertDoesNotThrow(() -> authService.iniciarSesion("111", "nueva"));
    }

    @Test
    void noCambiaLaClaveSiLaActualNoCoincide() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cambiarClave("111", "mala", "nueva", "nueva"));

        assertEquals("111", usuarios.buscarPorId("111").getClave());
    }

    @Test
    void noCambiaLaClaveSiLaConfirmacionNoCoincide() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cambiarClave("111", "111", "nueva", "distinta"));
    }

    @Test
    void noPermiteClaveNuevaVacia() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.cambiarClave("111", "111", "", ""));
    }

    @Test
    void cambiaLaClaveConSesionActiva() {
        authService.iniciarSesion("admin", "admin");

        authService.cambiarClave("admin", "nueva", "nueva");

        assertEquals("nueva", usuarios.buscarPorId("admin").getClave());
    }

    @Test
    void noCambiaLaClaveSinSesionActiva() {
        assertThrows(IllegalStateException.class,
                () -> authService.cambiarClave("admin", "nueva", "nueva"));
    }
}