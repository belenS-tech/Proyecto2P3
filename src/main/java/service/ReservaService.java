package service;

import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import repository.ReservaRepository;

import java.util.List;
import java.util.Objects;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ReservaService implements api.ReservaService {

    private final DisponibilidadService disponibilidadService;
    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public ReservaService(){
        this.reservaRepository = null;
        this.disponibilidadService = null;
        this.clock = Clock.systemDefaultZone();
    }

    public ReservaService(DisponibilidadService disponibilidadService, ReservaRepository reservaRepository) {
        this(disponibilidadService, reservaRepository, Clock.systemDefaultZone());
    }

    public ReservaService(DisponibilidadService disponibilidadService, ReservaRepository reservaRepository, Clock clock) {
        this.disponibilidadService = disponibilidadService;
        this.reservaRepository = reservaRepository;
        this.clock = Objects.requireNonNull(clock);
    }

    public ReservaService(ReservaRepository reservaRepository) {
        this(new DisponibilidadService(repository.Repositories.recursos(), reservaRepository), reservaRepository);
    }

    public List<Reserva> listarTodos() {
        return reservaRepository.listar();
    }

    public Reserva buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser nulo o vacío");
        }
        return reservaRepository.buscarPorId(id.trim());
    }

    public void registrar(Reserva reserva) {
        validarReservaBase(reserva, false);
        // El BackendServer coordina las escrituras de todos los clientes. Este
        // bloqueo también protege llamadas directas que comparten el repositorio.
        synchronized (reservaRepository) {
            repository.Repositories.atomic(reservaRepository, () -> {
                List<DetalleReserva> asignados = new ArrayList<>();
                List<String> noDisponibles = new ArrayList<>();
                for (DetalleReserva detalle : reserva.getDetalles()) {
                    Recurso libre = disponibilidadService.buscarRecursoDisponible(detalle.getCategoria(),
                            reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin());
                    if (libre == null) noDisponibles.add(detalle.getCategoria().getDescripcion());
                    else asignados.add(new DetalleReserva(libre.getCategoria(), libre));
                }
                if (!noDisponibles.isEmpty()) {
                    throw new IllegalArgumentException("No hay disponibilidad para: "
                            + String.join(", ", noDisponibles) + ". No se registró ningún recurso.");
                }
                List<DetalleReserva> solicitados = reserva.getDetalles();
                reserva.setDetalles(asignados);
                try { reservaRepository.guardar(reserva); }
                catch (RuntimeException e) { reserva.setDetalles(solicitados); throw e; }
            });
        }
    }

    public void actualizar(Reserva reserva) {
        validarReservaBase(reserva);
        Reserva guardada = reservaRepository.buscarPorId(reserva.getId().trim());
        if (guardada == null) {
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
        }
        // La fecha del cliente no puede convertir una reserva iniciada en futura.
        validarFutura(guardada, "modificar");
        reservaRepository.actualizar(reserva);
    }

    public void eliminar(Reserva reserva) {
        if (reserva == null || reserva.getId() == null || reserva.getId().isBlank())
            throw new IllegalArgumentException("Debe seleccionar una reserva.");
        Reserva guardada = reservaRepository.buscarPorId(reserva.getId().trim());
        if (guardada == null) {
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
        }
        validarCancelacion(guardada);
        reservaRepository.eliminar(guardada);
    }

    private void validarCancelacion(Reserva guardada) {
        validarFutura(guardada, "cancelar");
    }

    private void validarFutura(Reserva guardada, String accion) {
        if (!LocalDateTime.of(guardada.getFecha(), guardada.getHoraInicio()).isAfter(LocalDateTime.now(clock)))
            throw new IllegalArgumentException("Solo puede " + accion + " una reserva futura que no haya iniciado.");
    }

    private void validarReservaBase(Reserva reserva) {
        validarReservaBase(reserva, true);
    }

    private void validarReservaBase(Reserva reserva, boolean exigirRecurso) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (reserva.getId() == null || reserva.getId().isBlank()) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser nulo o vacío");
        }
        if (reserva.getActividad() == null || reserva.getActividad().isBlank()) {
            throw new IllegalArgumentException("La actividad no puede ser nula o vacía");
        }
        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (reserva.getHoraInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio no puede ser nula");
        }
        if (reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("La hora de fin no puede ser nula");
        }
        if (reserva.getFuncionarioId() == null || reserva.getFuncionarioId().isBlank()) {
            throw new IllegalArgumentException("El funcionario no puede ser nulo o vacío");
        }
        if (reserva.getEstado() == null) {
            throw new IllegalArgumentException("El estado de la reserva no puede ser nulo");
        }
        if (!reserva.getHoraInicio().isBefore(reserva.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        if (reserva.getDetalles() == null || reserva.getDetalles().isEmpty())
            throw new IllegalArgumentException("Debe seleccionar al menos un recurso.");
        java.util.Set<String> ids = new java.util.HashSet<>();
        java.util.Set<String> categorias = new java.util.HashSet<>();
        for (DetalleReserva detalle : reserva.getDetalles()) {
            if (detalle == null || detalle.getCategoria() == null || detalle.getCategoria().isEmpty())
                throw new IllegalArgumentException("Debe seleccionar una categoría válida.");
            if (!categorias.add(detalle.getCategoria().getId()))
                throw new IllegalArgumentException("No puede repetir una categoría en la reserva.");
            if (!exigirRecurso) continue;
            if (detalle.getRecurso() == null
                    || detalle.getRecurso().getId() == null || detalle.getRecurso().getId().isBlank())
                throw new IllegalArgumentException("El detalle de la reserva está incompleto.");
            if (!Objects.equals(detalle.getCategoria(), detalle.getRecurso().getCategoria()))
                throw new IllegalArgumentException("La categoría no corresponde al recurso.");
            if (!ids.add(detalle.getRecurso().getId()))
                throw new IllegalArgumentException("No puede repetir un recurso en la reserva.");
        }
    }
}
