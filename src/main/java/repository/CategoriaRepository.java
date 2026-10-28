package repository;

import model.CategoriaRecurso;
import java.util.List;


public interface CategoriaRepository {
    List<CategoriaRecurso> listarTodas();

        List<CategoriaRecurso> listarTodos();

        CategoriaRecurso buscarPorId(String id);

        void guardar(CategoriaRecurso categoria);

        void eliminar(String id);
    }
