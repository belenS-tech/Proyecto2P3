package integration;

import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import model.CategoriaRecurso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RecursoXmlRepository;
import repository.ReservaXmlRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaXmlRepositoryIT {

    private static final Path RECURSOS_PATH = Paths.get("src", "main", "resources", "data", "recursos.xml");
    private static final Path RESERVAS_PATH = Paths.get("src", "main", "resources", "data", "reservas.xml");

    private String recursosOriginal;
    private String reservasOriginal;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(RECURSOS_PATH.getParent());

        recursosOriginal = Files.exists(RECURSOS_PATH) ? Files.readString(RECURSOS_PATH) : null;
        reservasOriginal = Files.exists(RESERVAS_PATH) ? Files.readString(RESERVAS_PATH) : null;

        Files.writeString(RECURSOS_PATH, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><recursos/>");
        Files.writeString(RESERVAS_PATH, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><reservas/>");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (recursosOriginal == null) {
            Files.deleteIfExists(RECURSOS_PATH);
        } else {
            Files.writeString(RECURSOS_PATH, recursosOriginal);
        }

        if (reservasOriginal == null) {
            Files.deleteIfExists(RESERVAS_PATH);
        } else {
            Files.writeString(RESERVAS_PATH, reservasOriginal);
        }
    }

    @Test
    void guardarYBuscarPorIdDebePersistirReservaConDetalles() {
        RecursoXmlRepository recursoRepository = new RecursoXmlRepository();
        Recurso recurso = new Recurso("R-001", CategoriaRecurso.valueOf("SALA"), "Sala de reuniones");
        recursoRepository.guardar(recurso);

        ReservaXmlRepository reservaRepository = new ReservaXmlRepository(recursoRepository);
        Reserva reserva = new Reserva(
                "RES-001",
                "Capacitación",
                LocalDate.of(2026, 9, 20),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                "F-001",
                EstadoReserva.ACTIVA
        );
        reserva.setDetalles(List.of(new DetalleReserva(CategoriaRecurso.valueOf("SALA"), recurso)));

        reservaRepository.guardar(reserva);

        Reserva guardada = reservaRepository.buscarPorId("RES-001");
        assertNotNull(guardada);
        assertEquals("Capacitación", guardada.getActividad());
        assertEquals(1, guardada.getDetalles().size());
        assertEquals("R-001", guardada.getDetalles().get(0).getRecurso().getId());
    }

    @Test
    void actualizarYEliminarDebeModificarYQuitarReserva() {
        RecursoXmlRepository recursoRepository = new RecursoXmlRepository();
        Recurso recurso = new Recurso("R-001", CategoriaRecurso.valueOf("SALA"), "Sala de reuniones");
        recursoRepository.guardar(recurso);

        ReservaXmlRepository reservaRepository = new ReservaXmlRepository(recursoRepository);
        Reserva reserva = new Reserva(
                "RES-002",
                "Taller",
                LocalDate.of(2026, 9, 22),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                "F-002",
                EstadoReserva.ACTIVA
        );
        reserva.setDetalles(List.of(new DetalleReserva(CategoriaRecurso.valueOf("SALA"), recurso)));
        reservaRepository.guardar(reserva);

        reserva.setActividad("Revisión");
        reservaRepository.actualizar(reserva);
        assertEquals("Revisión", reservaRepository.buscarPorId("RES-002").getActividad());

        reservaRepository.eliminar(reserva);
        assertNull(reservaRepository.buscarPorId("RES-002"));
    }
}
