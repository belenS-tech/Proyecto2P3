package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface ReservaService {
    List<Reserva> listarTodos();
    Reserva buscarPorId(String id);
    void registrar(Reserva reserva);
    void actualizar(Reserva reserva);
    void eliminar(Reserva reserva);
}
