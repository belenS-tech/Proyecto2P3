package repository;

import model.Funcionario;
import java.util.List;

public class FuncionarioJdbcRepository extends JdbcSupport implements FuncionarioRepository {
    public List<Funcionario> listarTodos() {
        return query("SELECT id,nombre,telefono FROM p3_funcionarios ORDER BY id", r -> new Funcionario(r.getString(1),r.getString(2),r.getString(3)));
    }
    public Funcionario buscarPorId(String id) {
        return first(query("SELECT id,nombre,telefono FROM p3_funcionarios WHERE id=?", r -> new Funcionario(r.getString(1),r.getString(2),r.getString(3)), id));
    }
    public void guardar(Funcionario f) {
        execute("MERGE INTO p3_funcionarios t USING (SELECT ? id, ? nombre, ? telefono FROM dual) s ON (t.id=s.id) "
                + "WHEN MATCHED THEN UPDATE SET t.nombre=s.nombre,t.telefono=s.telefono WHEN NOT MATCHED THEN INSERT (id,nombre,telefono) VALUES (s.id,s.nombre,s.telefono)", f.getId(),f.getNombre(),f.getTelefono());
    }
    public void eliminar(String id) { execute("DELETE FROM p3_funcionarios WHERE id=?",id); }
}
