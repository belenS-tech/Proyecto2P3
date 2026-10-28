package unit;

import exception.AiReservationException;
import org.junit.jupiter.api.Test;
import service.AiReservationService;

import static org.junit.jupiter.api.Assertions.*;

public class AiReservationServiceTest {
    @Test
    void interpretarSolicitudNulaDebeLanzarExcepcion() {
        assertThrows(AiReservationException.class, () -> {
            AiReservationService aiReservationService = new AiReservationService();
            aiReservationService.interpretarSolicitud(null);
        });
    }
}
