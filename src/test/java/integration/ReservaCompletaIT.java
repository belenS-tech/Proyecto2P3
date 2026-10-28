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
import service.DisponibilidadService;
import service.ReservaService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaCompletaIT {

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
    void registrarReservaDisponibleDebeGuardarLaReservaEnElSistema() {
        RecursoXmlRepository recursoRepository = new RecursoXmlRepository();
        Recurso recurso = new Recurso("R-001", CategoriaRecurso.valueOf("SALA"), "Sala de reuniones");
        recursoRepository.guardar(recurso);

        ReservaXmlRepository reservaRepository = new ReservaXmlRepository(recursoRepository);
        DisponibilidadService disponibilidadService = new DisponibilidadService(recursoRepository, reservaRepository);
        ReservaService reservaService = new ReservaService(disponibilidadService, reservaRepository);

        Reserva reserva = new Reserva(
                "RES-100",
                "Reunión de equipo",
                LocalDate.of(2026, 9, 24),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                "F-001",
                EstadoReserva.ACTIVA
        );
        reserva.setDetalles(List.of(new DetalleReserva(CategoriaRecurso.valueOf("SALA"), recurso)));

        assertDoesNotThrow(() -> reservaService.registrar(reserva));
        assertNotNull(reservaRepository.buscarPorId("RES-100"));
    }

    @Test
    void registrarReservaCuandoElRecursoYaEstaOcupadoDebeLanzarExcepcion() {
        RecursoXmlRepository recursoRepository = new RecursoXmlRepository();
        Recurso recurso = new Recurso("R-001", CategoriaRecurso.valueOf("SALA"), "Sala de reuniones");
        recursoRepository.guardar(recurso);

        ReservaXmlRepository reservaRepository = new ReservaXmlRepository(recursoRepository);
        DisponibilidadService disponibilidadService = new DisponibilidadService(recursoRepository, reservaRepository);
        ReservaService reservaService = new ReservaService(disponibilidadService, reservaRepository);

        Reserva reserva1 = new Reserva(
                "RES-101",
                "Reunión de equipo",
                LocalDate.of(2026, 9, 24),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "F-001",
                EstadoReserva.ACTIVA
        );
        reserva1.setDetalles(List.of(new DetalleReserva(CategoriaRecurso.valueOf("SALA"), recurso)));
        reservaService.registrar(reserva1);

        Reserva reserva2 = new Reserva(
                "RES-102",
                "Otra reunión",
                LocalDate.of(2026, 9, 24),
                LocalTime.of(10, 30),
                LocalTime.of(11, 30),
                "F-002",
                EstadoReserva.ACTIVA
        );
        reserva2.setDetalles(List.of(new DetalleReserva(CategoriaRecurso.valueOf("SALA"), recurso)));

        assertThrows(IllegalArgumentException.class, () -> reservaService.registrar(reserva2));
    }
}
