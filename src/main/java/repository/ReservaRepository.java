package repository;

import model.Reserva;

import java.util.List;

public interface ReservaRepository {

    void guardar(Reserva reserva);

    List<Reserva> listar();

    Reserva buscarPorId(String id);

    void actualizar(Reserva reserva);

    void eliminar(Reserva reserva);
}