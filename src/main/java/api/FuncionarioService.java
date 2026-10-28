package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface FuncionarioService {
    List<Funcionario> listarTodos();
    List<Funcionario> buscar(String id, String nombre);
    Funcionario buscarPorId(String id);
    void incluir(String id, String nombre, String telefono);
    void modificar(String id, String nombre, String telefono);
    void eliminar(String id);
}
