package repository;

import model.Recurso;
import model.CategoriaRecurso;
import java.util.List;

public class RecursoJdbcRepository extends JdbcSupport implements RecursoRepository {
    private static final String SELECT = "SELECT r.id,r.descripcion,c.id categoria_id,c.descripcion categoria_descripcion FROM p3_recursos r JOIN p3_categorias c ON c.id=r.categoria_id";
    public List<Recurso> listar() { return consultar(" ORDER BY r.id"); }
    public List<Recurso> listarTodos() { return listar(); }
    public Recurso buscarPorId(String id) { return first(consultar(" WHERE r.id=?",id)); }
    public List<Recurso> buscarPorCategoria(String id) { return consultar(" WHERE r.categoria_id=? ORDER BY r.id",id); }
    private List<Recurso> consultar(String where, Object... values) {
        return query(SELECT + where,r -> new Recurso(r.getString("id"),new CategoriaRecurso(r.getString("categoria_id"),r.getString("categoria_descripcion")),r.getString("descripcion")),values);
    }
    public void guardar(Recurso r) {
        execute("INSERT INTO p3_recursos (id,categoria_id,descripcion) VALUES (?,?,?)",r.getId(),r.getCategoria().getId(),r.getDescripcion());
    }
    public void actualizar(Recurso r) {
        if (execute("UPDATE p3_recursos SET categoria_id=?,descripcion=? WHERE id=?",r.getCategoria().getId(),r.getDescripcion(),r.getId()) == 0)
            throw new IllegalArgumentException("No existe un recurso con ese ID.");
    }
    public void eliminar(Recurso r) { execute("DELETE FROM p3_recursos WHERE id=?",r.getId()); }
}
