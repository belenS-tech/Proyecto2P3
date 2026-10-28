package unit;

import org.junit.jupiter.api.Test;
import service.DisponibilidadService;

import static org.junit.jupiter.api.Assertions.*;

public class DisponibilidadServiceTest {
    @Test
    void buscarDisponibilidadConParametrosNulosDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            DisponibilidadService disponibilidadService = new DisponibilidadService();
            disponibilidadService.buscarRecursoDisponible(null, null, null, null);
        });
    }
}
