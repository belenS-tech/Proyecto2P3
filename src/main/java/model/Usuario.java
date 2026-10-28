package model;

public class Usuario implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String clave;
    private Rol rol;

    public Usuario() {
        this("", "", null);
    }

    public Usuario(String id, String clave, Rol rol) {
        this.id = id;
        this.clave = clave;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}