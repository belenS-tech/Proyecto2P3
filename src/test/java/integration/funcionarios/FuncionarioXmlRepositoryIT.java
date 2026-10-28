package integration.funcionarios;

import model.Funcionario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.FuncionarioXmlRepository;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioXmlRepositoryIT {

    private static final String ARCHIVO = "src/main/resources/data/funcionarios.xml";
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
    void guardaYRecuperaUnFuncionarioDelXml() {
        FuncionarioXmlRepository repositorio = new FuncionarioXmlRepository();
        repositorio.guardar(new Funcionario("111", "Juan Perez", "3323"));

        Funcionario recuperado = new FuncionarioXmlRepository().buscarPorId("111");

        assertNotNull(recuperado);
        assertEquals("Juan Perez", recuperado.getNombre());
        assertEquals("3323", recuperado.getTelefono());
    }

    @Test
    void elArchivoSeCreaAlGuardar() {
        new FuncionarioXmlRepository().guardar(new Funcionario("111", "Juan Perez", "3323"));

        assertTrue(new File(ARCHIVO).exists());
    }

    @Test
    void persisteVariosFuncionarios() {
        FuncionarioXmlRepository repositorio = new FuncionarioXmlRepository();
        repositorio.guardar(new Funcionario("111", "Juan Perez", "3323"));
        repositorio.guardar(new Funcionario("222", "Maria Perez", "222222"));
        repositorio.guardar(new Funcionario("333", "Carlos Ramirez", "8888888"));

        List<Funcionario> leidos = new FuncionarioXmlRepository().listarTodos();

        assertEquals(3, leidos.size());
    }

    @Test
    void guardarConElMismoIdReemplazaEnLugarDeDuplicar() {
        FuncionarioXmlRepository repositorio = new FuncionarioXmlRepository();
        repositorio.guardar(new Funcionario("111", "Juan Perez", "3323"));
        repositorio.guardar(new Funcionario("111", "Juan Perez Mora", "88887777"));

        List<Funcionario> leidos = new FuncionarioXmlRepository().listarTodos();

        assertEquals(1, leidos.size());
        assertEquals("Juan Perez Mora", leidos.get(0).getNombre());
    }

    @Test
    void eliminarQuitaElFuncionarioDelXml() {
        FuncionarioXmlRepository repositorio = new FuncionarioXmlRepository();
        repositorio.guardar(new Funcionario("111", "Juan Perez", "3323"));
        repositorio.eliminar("111");

        assertNull(new FuncionarioXmlRepository().buscarPorId("111"));
    }
}
