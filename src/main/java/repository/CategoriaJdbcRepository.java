package repository;

import model.CategoriaRecurso;
import java.util.List;

public class CategoriaJdbcRepository extends JdbcSupport implements CategoriaRepository {
    public List<CategoriaRecurso> listarTodas() { return listarTodos(); }
    public List<CategoriaRecurso> listarTodos() {
        return query("SELECT id, descripcion FROM p3_categorias ORDER BY id", r -> new CategoriaRecurso(r.getString(1), r.getString(2)));
    }
    public CategoriaRecurso buscarPorId(String id) {
        return first(query("SELECT id, descripcion FROM p3_categorias WHERE id = ?", r -> new CategoriaRecurso(r.getString(1), r.getString(2)), id));
    }
    public void guardar(CategoriaRecurso c) {
        execute("MERGE INTO p3_categorias t USING (SELECT ? id, ? descripcion FROM dual) s ON (t.id=s.id) "
                + "WHEN MATCHED THEN UPDATE SET t.descripcion=s.descripcion WHEN NOT MATCHED THEN INSERT (id,descripcion) VALUES (s.id,s.descripcion)", c.getId(), c.getDescripcion());
    }
    public void eliminar(String id) { execute("DELETE FROM p3_categorias WHERE id = ?", id); }
}
