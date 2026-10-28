package unit.funcionarios;

import model.Funcionario;
import repository.FuncionarioRepository;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioRepositoryEnMemoria implements FuncionarioRepository {

    private final List<Funcionario> funcionarios = new ArrayList<>();

    @Override
    public List<Funcionario> listarTodos() {
        return funcionarios;
    }

    @Override
    public Funcionario buscarPorId(String id) {
        return funcionarios.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void guardar(Funcionario funcionario) {
        funcionarios.removeIf(f -> f.getId().equals(funcionario.getId()));
        funcionarios.add(funcionario);
    }

    @Override
    public void eliminar(String id) {
        funcionarios.removeIf(f -> f.getId().equals(id));
    }
}