package model;

public class Recurso implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private CategoriaRecurso categoria;
    private String descripcion;

    public Recurso(String id, CategoriaRecurso categoria, String descripcion) {
        this.id = id;
        this.categoria = categoria;
        this.descripcion = descripcion;
    }

    public Recurso(int i, CategoriaRecurso o, String descripcion) {
        this.id = String.valueOf(i);
        this.categoria = o;
        this.descripcion = descripcion;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCategoria(CategoriaRecurso categoria) {
        this.categoria = categoria;
    }
    public CategoriaRecurso getCategoria() {
        return categoria;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
