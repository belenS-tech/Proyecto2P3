package integration.categorias;

import model.CategoriaRecurso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.CategoriaXmlRepository;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaXmlRepositoryIT {

    private static final String ARCHIVO = "src/main/resources/data/categorias.xml";
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
    void guardaYRecuperaUnaCategoriaDelXml() {
        CategoriaXmlRepository repositorio = new CategoriaXmlRepository();
        repositorio.guardar(new CategoriaRecurso("CAT-000001", "Sala de juntas"));

        // Un repositorio nuevo lee el archivo desde cero
        CategoriaXmlRepository otro = new CategoriaXmlRepository();
        CategoriaRecurso recuperada = otro.buscarPorId("CAT-000001");

        assertNotNull(recuperada);
        assertEquals("Sala de juntas", recuperada.getDescripcion());
    }

    @Test
    void elArchivoSeCreaAlGuardar() {
        CategoriaXmlRepository repositorio = new CategoriaXmlRepository();
        repositorio.guardar(new CategoriaRecurso("CAT-000001", "Laptop windows 11"));

        assertTrue(new File(ARCHIVO).exists());
    }

    @Test
    void persisteVariasCategorias() {
        CategoriaXmlRepository repositorio = new CategoriaXmlRepository();
        repositorio.guardar(new CategoriaRecurso("CAT-000001", "Sala de juntas"));
        repositorio.guardar(new CategoriaRecurso("CAT-000002", "Laptop windows 11"));
        repositorio.guardar(new CategoriaRecurso("CAT-000003", "Proyector"));

        List<CategoriaRecurso> leidas = new CategoriaXmlRepository().listarTodos();

        assertEquals(3, leidas.size());
    }

    @Test
    void guardarConElMismoIdReemplazaEnLugarDeDuplicar() {
        CategoriaXmlRepository repositorio = new CategoriaXmlRepository();
        repositorio.guardar(new CategoriaRecurso("CAT-000001", "Sala de juntas"));
        repositorio.guardar(new CategoriaRecurso("CAT-000001", "Sala de juntas grande"));

        List<CategoriaRecurso> leidas = new CategoriaXmlRepository().listarTodos();

        assertEquals(1, leidas.size());
        assertEquals("Sala de juntas grande", leidas.get(0).getDescripcion());
    }

    @Test
    void eliminarQuitaLaCategoriaDelXml() {
        CategoriaXmlRepository repositorio = new CategoriaXmlRepository();
        repositorio.guardar(new CategoriaRecurso("CAT-000001", "Sala de juntas"));
        repositorio.eliminar("CAT-000001");

        assertNull(new CategoriaXmlRepository().buscarPorId("CAT-000001"));
    }
}