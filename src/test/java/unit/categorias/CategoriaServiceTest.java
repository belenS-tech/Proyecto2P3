package unit.categorias;

import model.CategoriaRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.CategoriaService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaServiceTest {

    private CategoriaService servicio;

    @BeforeEach
    void prepararCadaPrueba() {
        servicio = new CategoriaService(new CategoriaRepositoryEnMemoria());
    }

    @Test
    void elPrimerIdGeneradoEsCat000001() {
        servicio.incluir("Sala de juntas");

        List<CategoriaRecurso> categorias = servicio.listarTodos();
        assertEquals(1, categorias.size());
        assertEquals("CAT-000001", categorias.get(0).getId());
    }

    @Test
    void losIdsSeGeneranConsecutivos() {
        servicio.incluir("Sala de juntas");
        servicio.incluir("Laptop windows 11");
        servicio.incluir("Proyector");

        assertEquals("CAT-000001", servicio.listarTodos().get(0).getId());
        assertEquals("CAT-000002", servicio.listarTodos().get(1).getId());
        assertEquals("CAT-000003", servicio.listarTodos().get(2).getId());
    }

    @Test
    void noPermiteDescripcionVacia() {
        assertThrows(IllegalArgumentException.class, () -> servicio.incluir(""));
        assertThrows(IllegalArgumentException.class, () -> servicio.incluir("   "));
        assertThrows(IllegalArgumentException.class, () -> servicio.incluir(null));
    }

    @Test
    void noPermiteDescripcionRepetida() {
        servicio.incluir("Sala de juntas");

        assertThrows(IllegalArgumentException.class,
                () -> servicio.incluir("Sala de juntas"));
    }

    @Test
    void laDescripcionRepetidaSeDetectaSinImportarMayusculas() {
        servicio.incluir("Sala de juntas");

        assertThrows(IllegalArgumentException.class,
                () -> servicio.incluir("SALA DE JUNTAS"));
    }

    @Test
    void modificarConservaElId() {
        servicio.incluir("Sala de juntas");
        String idOriginal = servicio.listarTodos().get(0).getId();

        servicio.modificar(idOriginal, "Sala de juntas grande");

        CategoriaRecurso modificada = servicio.buscarPorId(idOriginal);
        assertNotNull(modificada);
        assertEquals(idOriginal, modificada.getId());
        assertEquals("Sala de juntas grande", modificada.getDescripcion());
    }

    @Test
    void noModificaUnaCategoriaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificar("CAT-999999", "Cualquier cosa"));
    }

    @Test
    void laBusquedaFiltraPorCoincidenciaParcial() {
        servicio.incluir("Sala de juntas");
        servicio.incluir("Sala para 10 personas");
        servicio.incluir("Laptop windows 11");

        assertEquals(2, servicio.buscar("sala").size());
        assertEquals(1, servicio.buscar("laptop").size());
        assertEquals(3, servicio.buscar("").size());
    }

    @Test
    void eliminarQuitaLaCategoria() {
        servicio.incluir("Sala de juntas");
        String id = servicio.listarTodos().get(0).getId();

        servicio.eliminar(id);

        assertTrue(servicio.listarTodos().isEmpty());
        assertNull(servicio.buscarPorId(id));
    }

    @Test
    void noEliminaUnaCategoriaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminar("CAT-999999"));
    }
}