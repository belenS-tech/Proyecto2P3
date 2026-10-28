package integration.login;

import model.Rol;
import model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.UsuarioXmlRepository;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioXmlRepositoryIT {

    private static final String ARCHIVO = "src/main/resources/data/usuarios.xml";
    private File respaldo;

    @BeforeEach
    void respaldarArchivoReal() {
        File original = new File(ARCHIVO);
        if (original.exists()) {
            respaldo = new File(ARCHIVO + ".bak");
            if (!original.renameTo(respaldo)) throw new IllegalStateException("No se pudo respaldar " + ARCHIVO);
        }
    }

    @AfterEach
    void restaurarArchivoReal() {
        new File(ARCHIVO).delete();
        if (respaldo != null && respaldo.exists()) {
            if (!respaldo.renameTo(new File(ARCHIVO))) throw new IllegalStateException("No se pudo restaurar " + ARCHIVO);
        }
    }

    @Test
    void creaElAdminPorDefectoCuandoNoExisteElArchivo() {
        UsuarioXmlRepository repositorio = new UsuarioXmlRepository();

        Usuario admin = repositorio.buscarPorId("admin");
        assertNotNull(admin);
        assertEquals("admin", admin.getClave());
        assertEquals(Rol.ADMINISTRADOR, admin.getRol());
    }

    @Test
    void guardaYRecuperaUnUsuarioDelXml() {
        UsuarioXmlRepository repositorio = new UsuarioXmlRepository();
        repositorio.guardar(new Usuario("111", "111", Rol.FUNCIONARIO));

        Usuario recuperado = new UsuarioXmlRepository().buscarPorId("111");

        assertNotNull(recuperado);
        assertEquals("111", recuperado.getClave());
        assertEquals(Rol.FUNCIONARIO, recuperado.getRol());
    }

    @Test
    void elRolSePersisteCorrectamente() {
        UsuarioXmlRepository repositorio = new UsuarioXmlRepository();
        repositorio.guardar(new Usuario("jefe", "1234", Rol.ADMINISTRADOR));
        repositorio.guardar(new Usuario("empleado", "1234", Rol.FUNCIONARIO));

        UsuarioXmlRepository otro = new UsuarioXmlRepository();
        assertEquals(Rol.ADMINISTRADOR, otro.buscarPorId("jefe").getRol());
        assertEquals(Rol.FUNCIONARIO, otro.buscarPorId("empleado").getRol());
    }

    @Test
    void elCambioDeClaveQuedaGuardado() {
        UsuarioXmlRepository repositorio = new UsuarioXmlRepository();
        repositorio.guardar(new Usuario("111", "111", Rol.FUNCIONARIO));

        Usuario usuario = repositorio.buscarPorId("111");
        usuario.setClave("nueva");
        repositorio.guardar(usuario);

        assertEquals("nueva", new UsuarioXmlRepository().buscarPorId("111").getClave());
    }

    @Test
    void eliminarQuitaElUsuarioDelXml() {
        UsuarioXmlRepository repositorio = new UsuarioXmlRepository();
        repositorio.guardar(new Usuario("111", "111", Rol.FUNCIONARIO));
        repositorio.eliminar("111");

        assertNull(new UsuarioXmlRepository().buscarPorId("111"));
    }

    @Test
    void persisteVariosUsuarios() {
        UsuarioXmlRepository repositorio = new UsuarioXmlRepository();
        repositorio.guardar(new Usuario("111", "111", Rol.FUNCIONARIO));
        repositorio.guardar(new Usuario("222", "222", Rol.FUNCIONARIO));

        List<Usuario> leidos = new UsuarioXmlRepository().listarTodos();

        // admin por defecto + los dos agregados
        assertEquals(3, leidos.size());
    }
}