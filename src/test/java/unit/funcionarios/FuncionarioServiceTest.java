package unit.funcionarios;

import model.Funcionario;
import model.Rol;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FuncionarioService;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioServiceTest {

    private FuncionarioService servicio;
    private UsuarioRepositoryEnMemoria usuarios;

    @BeforeEach
    void prepararCadaPrueba() {
        usuarios = new UsuarioRepositoryEnMemoria();
        servicio = new FuncionarioService(new FuncionarioRepositoryEnMemoria(), usuarios,
                new unit.estadisticas.ReservaRepositoryEnMemoria());
    }

    @Test
    void alIncluirSeCreaElFuncionario() {
        servicio.incluir("111", "Juan Perez", "3323");

        Funcionario funcionario = servicio.buscarPorId("111");
        assertNotNull(funcionario);
        assertEquals("Juan Perez", funcionario.getNombre());
        assertEquals("3323", funcionario.getTelefono());
    }

    @Test
    void alIncluirSeCreaSuUsuarioConClaveIgualAlId() {
        servicio.incluir("111", "Juan Perez", "3323");

        Usuario usuario = usuarios.buscarPorId("111");
        assertNotNull(usuario);
        assertEquals("111", usuario.getClave());
        assertEquals(Rol.FUNCIONARIO, usuario.getRol());
    }

    @Test
    void noPermiteIdRepetido() {
        servicio.incluir("111", "Juan Perez", "3323");

        assertThrows(IllegalArgumentException.class,
                () -> servicio.incluir("111", "Maria Perez", "222222"));
    }

    @Test
    void noPermiteCamposVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.incluir("", "Juan Perez", "3323"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.incluir("111", "", "3323"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.incluir("111", "Juan Perez", ""));
    }

    @Test
    void modificarCambiaNombreYTelefonoPeroNoElId() {
        servicio.incluir("111", "Juan Perez", "3323");

        servicio.modificar("111", "Juan Perez Mora", "88887777");

        Funcionario funcionario = servicio.buscarPorId("111");
        assertEquals("111", funcionario.getId());
        assertEquals("Juan Perez Mora", funcionario.getNombre());
        assertEquals("88887777", funcionario.getTelefono());
    }

    @Test
    void noModificaUnFuncionarioInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificar("999", "Nadie", "0000"));
    }

    @Test
    void eliminarBorraTambienSuUsuario() {
        servicio.incluir("111", "Juan Perez", "3323");

        servicio.eliminar("111");

        assertNull(servicio.buscarPorId("111"));
        assertNull(usuarios.buscarPorId("111"));
    }

    @Test
    void noEliminaUnFuncionarioInexistente() {
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminar("999"));
    }

    @Test
    void laBusquedaPorIdEsExacta() {
        servicio.incluir("111", "Juan Perez", "3323");
        servicio.incluir("222", "Maria Perez", "222222");

        assertEquals(1, servicio.buscar("111", "").size());
        assertEquals(0, servicio.buscar("999", "").size());
    }

    @Test
    void laBusquedaPorNombreEsParcialYSinMayusculas() {
        servicio.incluir("111", "Juan Perez", "3323");
        servicio.incluir("222", "Maria Perez", "222222");
        servicio.incluir("333", "Carlos Ramirez", "8888888");

        assertEquals(2, servicio.buscar("", "perez").size());
        assertEquals(1, servicio.buscar("", "CARLOS").size());
        assertEquals(3, servicio.buscar("", "").size());
    }
}
