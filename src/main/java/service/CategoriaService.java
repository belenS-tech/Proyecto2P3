package service;

import model.CategoriaRecurso;
import model.Recurso;
import model.Reserva;
import repository.CategoriaRepository;
import repository.RecursoRepository;
import repository.ReservaRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CategoriaService implements api.CategoriaService {

    private static final String PREFIJO = "CAT-";
    private static final int LARGO_NUMERO = 6;

    private final CategoriaRepository categoriaRepository;
    private final RecursoRepository recursoRepository;
    private final ReservaRepository reservaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this(categoriaRepository, null, null);
    }

    public CategoriaService(CategoriaRepository categoriaRepository,
                            RecursoRepository recursoRepository,
                            ReservaRepository reservaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.recursoRepository = recursoRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<CategoriaRecurso> listarTodos() {
        return categoriaRepository.listarTodos();
    }

    public List<CategoriaRecurso> buscar(String descripcion) {
        return categoriaRepository.listarTodos().stream()
                .filter(c -> descripcion == null || descripcion.isBlank()
                        || c.getDescripcion().toLowerCase().contains(descripcion.trim().toLowerCase()))
                .collect(Collectors.toList());
    }

    public CategoriaRecurso buscarPorId(String id) {
        return categoriaRepository.buscarPorId(id);
    }

    public void incluir(String descripcion) {
        validarDescripcion(descripcion);
        if (existeDescripcion(descripcion, null))
            throw new IllegalArgumentException("Ya existe una categoría con esa descripción.");
        categoriaRepository.guardar(new CategoriaRecurso(generarId(), descripcion.trim()));
    }

    public void modificar(String id, String descripcion) {
        validarDescripcion(descripcion);
        CategoriaRecurso categoria = categoriaRepository.buscarPorId(id);
        if (categoria == null)
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        if (existeDescripcion(descripcion, id))
            throw new IllegalArgumentException("Ya existe otra categoría con esa descripción.");
        categoria.setDescripcion(descripcion.trim());
        categoriaRepository.guardar(categoria);
    }

    public void eliminar(String id) {
        repository.Repositories.atomic(categoriaRepository, () -> eliminarInterno(id));
    }

    private void eliminarInterno(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        if (categoriaRepository.buscarPorId(id) == null)
            throw new IllegalArgumentException("No existe una categoría con ese ID.");

        // Cascada: eliminar reservas que usan recursos de esta categoría, luego los recursos
        if (recursoRepository != null && reservaRepository != null) {
            List<Recurso> recursos = recursoRepository.buscarPorCategoria(id);
            for (Recurso recurso : recursos) {
                List<Reserva> reservasConRecurso = reservaRepository.listar().stream()
                        .filter(r -> r.getDetalles().stream()
                                .anyMatch(d -> d.getRecurso() != null
                                        && d.getRecurso().getId().equals(recurso.getId())))
                        .collect(java.util.stream.Collectors.toList());
                reservasConRecurso.forEach(reservaRepository::eliminar);
                recursoRepository.eliminar(recurso);
            }
        }

        categoriaRepository.eliminar(id);
    }

    private String generarId() {
        int mayor = 0;
        for (CategoriaRecurso c : categoriaRepository.listarTodos()) {
            String cid = c.getId();
            if (cid != null && cid.startsWith(PREFIJO)) {
                try {
                    int n = Integer.parseInt(cid.substring(PREFIJO.length()));
                    if (n > mayor) mayor = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return PREFIJO + String.format("%0" + LARGO_NUMERO + "d", mayor + 1);
    }

    private boolean existeDescripcion(String descripcion, String idExcluido) {
        return categoriaRepository.listarTodos().stream()
                .filter(c -> idExcluido == null || !c.getId().equals(idExcluido))
                .anyMatch(c -> c.getDescripcion().equalsIgnoreCase(descripcion.trim()));
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank())
            throw new IllegalArgumentException("La descripción no puede estar vacía.");
    }
}
