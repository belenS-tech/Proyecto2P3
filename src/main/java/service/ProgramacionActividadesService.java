package service;


import model.FilaActividad;
import model.EstadoReserva;
import model.Reserva;
import model.ResultadoProgramacionActividades;
import repository.ReservaRepository;
import repository.FuncionarioRepository;
import model.Funcionario;
import util.DateUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Construye la matriz de programación semanal: filas = horas del
 * día, columnas = días de la semana de referencia, celdas =
 * actividades programadas (puede haber varias por celda).
*/

public class ProgramacionActividadesService implements api.ProgramacionActividadesService {

    private final ReservaRepository reservaRepo;
    private final FuncionarioRepository funcionarioRepo;

    public ProgramacionActividadesService(ReservaRepository reservaRepo) {
        this(reservaRepo, repository.Repositories.funcionarios());
    }

    public ProgramacionActividadesService(ReservaRepository reservaRepo, FuncionarioRepository funcionarioRepo) {
        this.reservaRepo = reservaRepo;
        this.funcionarioRepo = funcionarioRepo;
    }

    public ResultadoProgramacionActividades construirMatrizSemana(LocalDate fechaReferencia) {
        if (fechaReferencia == null) {
            throw new IllegalArgumentException("Debe seleccionar una fecha de referencia.");
        }

        LocalDate inicio = DateUtils.inicioSemana(fechaReferencia);
        LocalDate fin = DateUtils.finSemana(fechaReferencia);

        List<LocalDate> dias = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            dias.add(d);
        }

        List<Reserva> reservasSemana = reservaRepo.listar().stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .filter(r -> !r.getFecha().isBefore(inicio) && !r.getFecha().isAfter(fin))
                .collect(Collectors.toList());

        List<FilaActividad> filas = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            LocalTime hora = LocalTime.of(h, 0);
            FilaActividad fila = new FilaActividad(hora.toString());
            for (LocalDate dia : dias) {
                fila.setValor(dia, buscarActividad(reservasSemana, dia, hora));
            }
            filas.add(fila);
        }

        return new ResultadoProgramacionActividades(dias, filas);
    }

    private String buscarActividad(List<Reserva> reservasSemana, LocalDate dia, LocalTime hora) {
        List<String> actividadesEnCelda = new ArrayList<>();

        for (Reserva reserva : reservasSemana) {
            if (!reserva.getFecha().equals(dia)) continue;

            boolean estaEnRango = DateUtils.ocupaHora(reserva.getHoraInicio(), reserva.getHoraFin(), hora);
            if (!estaEnRango) continue;

            Funcionario funcionario = funcionarioRepo.buscarPorId(reserva.getFuncionarioId());
            actividadesEnCelda.add(reserva.getActividad() + " ("
                    + (funcionario == null ? reserva.getFuncionarioId() : funcionario.getNombre()) + ")");
        }

        return String.join(" | ", actividadesEnCelda);
    }
}

