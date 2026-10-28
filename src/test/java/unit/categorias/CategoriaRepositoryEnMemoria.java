package unit.categorias;

import model.CategoriaRecurso;
import repository.CategoriaRepository;

import java.util.ArrayList;
import java.util.List;

public class CategoriaRepositoryEnMemoria implements CategoriaRepository {

    private final List<CategoriaRecurso> categorias = new ArrayList<>();

    @Override
    public List<CategoriaRecurso> listarTodos() {
        return categorias;
    }

    @Override
    public CategoriaRecurso buscarPorId(String id) {
        return categorias.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {
        categorias.removeIf(c -> c.getId().equals(categoria.getId()));
        categorias.add(categoria);
    }

    @Override
    public void eliminar(String id) {
        categorias.removeIf(c -> c.getId().equals(id));
    }

    @Override
    public List<CategoriaRecurso> listarTodas() {
        return categorias;
    }

}