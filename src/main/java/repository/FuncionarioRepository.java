package repository;

import model.Funcionario;

import java.util.List;

public interface FuncionarioRepository {

    List<Funcionario> listarTodos();

    Funcionario buscarPorId(String id);

    void guardar(Funcionario funcionario);

    void eliminar(String id);
}