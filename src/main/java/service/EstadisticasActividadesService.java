package service;

import model.EstadoReserva;
import model.Reserva;
import repository.ReservaRepository;
import util.DateUtils;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class EstadisticasActividadesService implements api.EstadisticasActividadesService {

    private final ReservaRepository reservaRepo;

    public EstadisticasActividadesService(ReservaRepository reservaRepo) {
        this.reservaRepo = reservaRepo;
    }

    public LinkedHashMap<String, Integer> calcular(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar ambas fechas, desde y hasta.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }

        List<Reserva> reservas = reservaRepo.listar().stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .collect(Collectors.toList());

        TreeMap<LocalDate, Integer> conteoPorInicioSemana = new TreeMap<>();
        for (LocalDate semana = DateUtils.inicioSemana(desde);
             !semana.isAfter(hasta); semana = semana.plusWeeks(1)) {
            conteoPorInicioSemana.put(semana, 0);
        }
        for (Reserva reserva : reservas) {
            LocalDate inicioSemana = DateUtils.inicioSemana(reserva.getFecha());
            conteoPorInicioSemana.merge(inicioSemana, 1, Integer::sum);
        }

        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, Integer> entry : conteoPorInicioSemana.entrySet()) {
            resultado.put(DateUtils.etiquetaSemana(entry.getKey()), entry.getValue());
        }
        return resultado;
    }
}
