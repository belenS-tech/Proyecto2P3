package service;

import model.Recurso;
import model.CategoriaRecurso;
import repository.RecursoRepository;

import java.util.List;

public class RecursoService implements api.RecursoService {
    private final RecursoRepository repository;

    public RecursoService() {
        this.repository = null;
    }

    public RecursoService(RecursoRepository repository) {
        this.repository = repository;
    }

    public List<Recurso> listarTodos() {
        return repository.listar();
    }

    public Recurso buscarPorId(String id) {
        validarId(id);
        return repository.buscarPorId(id.trim());
    }

    public void registrar(Recurso recurso) {
        validarRecurso(recurso);
        if (repository.buscarPorId(recurso.getId().trim()) != null) {
            throw new IllegalArgumentException("El ID del recurso ya existe.");
        }
        repository.guardar(recurso);
    }

    public void actualizar(Recurso recurso) {
        validarRecurso(recurso);
        if (repository.buscarPorId(recurso.getId().trim()) == null) {
            throw new IllegalArgumentException("No existe un recurso con ese ID.");
        }
        repository.actualizar(recurso);
    }

    public void eliminar(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo.");
        }
        validarId(recurso.getId());
        if (repository.buscarPorId(recurso.getId().trim()) == null) {
            throw new IllegalArgumentException("No existe un recurso con ese ID.");
        }
        repository.eliminar(recurso);
    }

    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo.");
        }
        validarId(recurso.getId());

        CategoriaRecurso categoria = recurso.getCategoria();
        if (categoria == null || categoria.isEmpty()) {
            throw new IllegalArgumentException("La categoría del recurso no puede ser nula o vacía.");
        }

        if (recurso.getDescripcion() == null || recurso.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del recurso no puede ser nula o vacía.");
        }
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del recurso no puede ser nulo o vacío.");
        }
    }
}
