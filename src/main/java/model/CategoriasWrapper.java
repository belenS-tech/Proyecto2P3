package model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "categorias")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoriasWrapper implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "categoria")
    private List<CategoriaRecurso> categorias;

    public CategoriasWrapper() {
        this.categorias = new ArrayList<>();
    }

    public List<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
    }
}