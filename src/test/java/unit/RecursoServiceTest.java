package unit;
import org.junit.jupiter.api.Test;
import service.RecursoService;

import static org.junit.jupiter.api.Assertions.*;

public class RecursoServiceTest {
    @Test
    void registrarRecursoNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecursoService recursoService = new RecursoService();
            recursoService.registrar(null);
        });
    }

    @Test
    void buscarRecursoPorIdNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecursoService recursoService = new RecursoService();
            recursoService.buscarPorId(null);
        });
    }

    @Test
    void eliminarRecursoNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecursoService recursoService = new RecursoService();
            recursoService.eliminar(null);
        });
    }

    @Test
    void idNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            RecursoService recursoService = new RecursoService();
            recursoService.buscarPorId(null);
        });
    }
}
