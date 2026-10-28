package service;

import model.*;
import repository.RecursoRepository;
import repository.ReservaRepository;
import repository.FuncionarioRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Construye la matriz de calendarización
*/
public class CalendarizacionRecursosService implements api.CalendarizacionRecursosService {

    private final RecursoRepository recursoRepo;
    private final ReservaRepository reservaRepo;
    private final FuncionarioRepository funcionarioRepo;

    public CalendarizacionRecursosService(RecursoRepository recursoRepo, ReservaRepository reservaRepo) {
        this(recursoRepo, reservaRepo, repository.Repositories.funcionarios());
    }

    public CalendarizacionRecursosService(RecursoRepository recursoRepo, ReservaRepository reservaRepo,
                                         FuncionarioRepository funcionarioRepo) {
        this.recursoRepo = recursoRepo;
        this.reservaRepo = reservaRepo;
        this.funcionarioRepo = funcionarioRepo;
    }

    public ResultadoCalendarizacion construirMatriz(LocalDate fecha, String categoriaId) {
        if (fecha == null) {
            throw new IllegalArgumentException("Debe seleccionar una fecha.");
        }
        if (categoriaId == null || categoriaId.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }

        List<Recurso> recursos = recursoRepo.buscarPorCategoria(categoriaId);
        if (recursos.isEmpty()) {
            return new ResultadoCalendarizacion(recursos, new ArrayList<>());
        }

        List<Reserva> reservasDelDia = reservaRepo.listar().stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .filter(r -> fecha.equals(r.getFecha()))
                .collect(Collectors.toList());

        List<FilaCalendarizacion> filas = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            LocalTime hora = LocalTime.of(h, 0);
            FilaCalendarizacion fila = new FilaCalendarizacion(hora.toString());
            for (Recurso recurso : recursos) {
                fila.setValor(recurso.getId(), buscarOcupacion(reservasDelDia, hora, categoriaId, recurso.getId()));
            }
            filas.add(fila);
        }

        return new ResultadoCalendarizacion(recursos, filas);
    }

    private String buscarOcupacion(List<Reserva> reservasDelDia, LocalTime hora,
                                   String categoriaId, String recursoId) {
        List<String> ocupaciones = new ArrayList<>();
        for (Reserva reserva : reservasDelDia) {
            boolean estaEnRango = util.DateUtils.ocupaHora(reserva.getHoraInicio(), reserva.getHoraFin(), hora);
            if (!estaEnRango) continue;

            for (DetalleReserva detalle : reserva.getDetalles()) {
                boolean mismaCategoria = detalle.getCategoria() != null
                        && categoriaId.equals(detalle.getCategoria().getId());
                boolean mismoRecurso = detalle.getRecurso() != null
                        && recursoId.equals(detalle.getRecurso().getId());
                if (mismaCategoria && mismoRecurso) {
                    Funcionario funcionario = funcionarioRepo.buscarPorId(reserva.getFuncionarioId());
                    ocupaciones.add(reserva.getActividad() + " - "
                            + (funcionario == null ? reserva.getFuncionarioId() : funcionario.getNombre()));
                    break;
                }
            }
        }
        return String.join(" | ", ocupaciones);
    }
}
