package integration;

import model.Recurso;
import model.CategoriaRecurso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RecursoXmlRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursoXmlRepositoryIT {

    private static final Path RECURSOS_PATH = Paths.get("src", "main", "resources", "data", "recursos.xml");
    private String contenidoOriginal;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(RECURSOS_PATH.getParent());
        contenidoOriginal = Files.exists(RECURSOS_PATH)
                ? Files.readString(RECURSOS_PATH)
                : null;
        Files.writeString(RECURSOS_PATH, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><recursos/>");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (contenidoOriginal == null) {
            Files.deleteIfExists(RECURSOS_PATH);
        } else {
            Files.writeString(RECURSOS_PATH, contenidoOriginal);
        }
    }

    @Test
    void guardarYBuscarPorIdDebePersistirRecurso() {
        RecursoXmlRepository repository = new RecursoXmlRepository();
        Recurso recurso = new Recurso("R-001", CategoriaRecurso.valueOf("SALA"), "Sala de reuniones");

        repository.guardar(recurso);

        Recurso guardado = repository.buscarPorId("R-001");
        assertNotNull(guardado);
        assertEquals("SALA", guardado.getCategoria().getId());
        assertEquals("Sala de reuniones", guardado.getDescripcion());
    }

    @Test
    void buscarPorCategoriaDebeFiltrarRecursosDeLaCategoria() {
        RecursoXmlRepository repository = new RecursoXmlRepository();
        repository.guardar(new Recurso("R-001", CategoriaRecurso.valueOf("SALA"), "Sala principal"));
        repository.guardar(new Recurso("R-002", CategoriaRecurso.valueOf("PROYECTOR"), "Proyector portátil"));

        List<Recurso> salas = repository.buscarPorCategoria("SALA");

        assertEquals(1, salas.size());
        assertEquals("R-001", salas.get(0).getId());
    }
}
