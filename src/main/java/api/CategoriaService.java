package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface CategoriaService {
    List<CategoriaRecurso> listarTodos();
    List<CategoriaRecurso> buscar(String descripcion);
    CategoriaRecurso buscarPorId(String id);
    void incluir(String descripcion);
    void modificar(String id, String descripcion);
    void eliminar(String id);
}
