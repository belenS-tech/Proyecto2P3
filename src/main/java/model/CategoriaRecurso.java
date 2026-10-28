package model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "categoria")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoriaRecurso implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String descripcion;

    public CategoriaRecurso() {
    }

    public CategoriaRecurso(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEmpty() {
        return (id == null || id.isBlank())
                || (descripcion == null || descripcion.isBlank());
    }

    public static CategoriaRecurso valueOf(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }
        return new CategoriaRecurso(categoria.trim(), categoria.trim());
    }

    @Override
    public String toString() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoriaRecurso)) return false;
        CategoriaRecurso that = (CategoriaRecurso) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}