package service;

import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import model.CategoriaRecurso;
import repository.RecursoRepository;
import repository.ReservaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;


public class DisponibilidadService implements api.DisponibilidadService {

    private final RecursoRepository recursoRepository;
    private final ReservaRepository reservaRepository;
    public DisponibilidadService(){
        this.reservaRepository = null;
        this.recursoRepository = null;
    }

    public DisponibilidadService(RecursoRepository recursoRepository, ReservaRepository reservaRepository) {
        this.recursoRepository = recursoRepository;
        this.reservaRepository = reservaRepository;
    }

    boolean estaDisponible(Recurso recurso, LocalDate fecha, LocalTime inicio, LocalTime fin){
        List<Reserva> reservas = reservaRepository.listar();
        for (Reserva reserva : reservas) {
            if (reserva.getFecha().equals(fecha) && reserva.getEstado() == EstadoReserva.ACTIVA) {
                for (DetalleReserva detalle : reserva.getDetalles()) {
                    if (detalle.getRecurso().getId().equals(recurso.getId())) {
                        if (inicio.isBefore(reserva.getHoraFin()) && fin.isAfter(reserva.getHoraInicio())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public Recurso buscarRecursoDisponible(CategoriaRecurso categoria, LocalDate fecha, LocalTime inicio, LocalTime fin){
        if(categoria == null || fecha == null || inicio == null || fin == null){
            throw new IllegalArgumentException("Los parámetros no pueden ser nulos");
        }
        List<Recurso> recursos = recursoRepository.listar().stream()
                .filter(recurso -> Objects.equals(recurso.getCategoria(), categoria))
                .collect(java.util.stream.Collectors.toList());

         for (Recurso recurso : recursos) {
            if (estaDisponible(recurso, fecha, inicio, fin)) {
                return recurso;
            }
        }
        return null;
    }
}
