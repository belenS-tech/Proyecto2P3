package repository;

import model.Usuario;
import model.Rol;
import java.util.List;

public class UsuarioJdbcRepository extends JdbcSupport implements UsuarioRepository {
    public List<Usuario> listarTodos() {
        return query("SELECT id,clave,rol FROM p3_usuarios ORDER BY id", r -> new Usuario(r.getString(1),r.getString(2),Rol.valueOf(r.getString(3))));
    }
    public Usuario buscarPorId(String id) {
        return first(query("SELECT id,clave,rol FROM p3_usuarios WHERE id=?", r -> new Usuario(r.getString(1),r.getString(2),Rol.valueOf(r.getString(3))),id));
    }
    public void guardar(Usuario u) {
        execute("MERGE INTO p3_usuarios t USING (SELECT ? id, ? clave, ? rol FROM dual) s ON (t.id=s.id) "
                + "WHEN MATCHED THEN UPDATE SET t.clave=s.clave,t.rol=s.rol WHEN NOT MATCHED THEN INSERT (id,clave,rol) VALUES (s.id,s.clave,s.rol)",u.getId(),u.getClave(),u.getRol().name());
    }
    public void eliminar(String id) { execute("DELETE FROM p3_usuarios WHERE id=?",id); }
}
