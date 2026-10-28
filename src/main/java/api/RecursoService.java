package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface RecursoService {
    List<Recurso> listarTodos();
    Recurso buscarPorId(String id);
    void registrar(Recurso recurso);
    void actualizar(Recurso recurso);
    void eliminar(Recurso recurso);
}
