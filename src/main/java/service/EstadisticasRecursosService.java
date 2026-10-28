package service;

import model.DetalleReserva;
import model.EstadoReserva;
import model.Reserva;
import model.CategoriaRecurso;
import repository.ReservaRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class EstadisticasRecursosService implements api.EstadisticasRecursosService {

    private final ReservaRepository reservaRepository;

    public EstadisticasRecursosService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }


    public Map<String, Integer> contarPorCategoria(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);

        Map<String, Integer> conteo = new LinkedHashMap<>();

        for (Reserva reserva : reservaRepository.listar()) {
            if (!estaEnPeriodo(reserva, desde, hasta)) {
                continue;
            }
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                continue;
            }

            for (DetalleReserva detalle : reserva.getDetalles()) {
                String descripcion = descripcionDe(detalle);
                if (descripcion != null) {
                    conteo.merge(descripcion, 1, Integer::sum);
                }
            }
        }

        return conteo;
    }

    private boolean estaEnPeriodo(Reserva reserva, LocalDate desde, LocalDate hasta) {
        LocalDate fecha = reserva.getFecha();
        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private String descripcionDe(DetalleReserva detalle) {
        CategoriaRecurso categoria = detalle.getCategoria();
        if (categoria == null) {
            return null;
        }
        return categoria.getDescripcion() != null
                ? categoria.getDescripcion()
                : categoria.getId();
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las fechas desde y hasta.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }
}