package unit.funcionarios;

import model.Usuario;
import repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final List<Usuario> usuarios = new ArrayList<>();

    @Override
    public List<Usuario> listarTodos() {
        return usuarios;
    }

    @Override
    public Usuario buscarPorId(String id) {
        return usuarios.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void guardar(Usuario usuario) {
        usuarios.removeIf(u -> u.getId().equals(usuario.getId()));
        usuarios.add(usuario);
    }

    @Override
    public void eliminar(String id) {
        usuarios.removeIf(u -> u.getId().equals(id));
    }
}