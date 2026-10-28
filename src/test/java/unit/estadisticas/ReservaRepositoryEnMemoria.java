package unit.estadisticas;

import model.Reserva;
import repository.ReservaRepository;

import java.util.ArrayList;
import java.util.List;

public class ReservaRepositoryEnMemoria implements ReservaRepository {

    private final List<Reserva> reservas = new ArrayList<>();

    @Override
    public void guardar(Reserva reserva) {
        reservas.add(reserva);
    }

    @Override
    public List<Reserva> listar() {
        return reservas;
    }

    @Override
    public Reserva buscarPorId(String id) {
        return reservas.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void actualizar(Reserva reserva) {
        reservas.removeIf(r -> r.getId().equals(reserva.getId()));
        reservas.add(reserva);
    }

    @Override
    public void eliminar(Reserva reserva) {
        reservas.removeIf(r -> r.getId().equals(reserva.getId()));
    }
}