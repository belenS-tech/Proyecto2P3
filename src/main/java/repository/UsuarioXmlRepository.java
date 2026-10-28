package repository;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.Usuario;
import model.Rol;
import model.UsuariosWrapper;

import util.RutaArchivos;
import java.io.File;
import java.util.List;

public class UsuarioXmlRepository implements UsuarioRepository {

    private static final String ARCHIVO = "src/main/resources/data/usuarios.xml";
    private UsuariosWrapper datos;

    public UsuarioXmlRepository() {
        cargar();
    }

    private void cargar() {
        File archivo = RutaArchivos.get(ARCHIVO);
        if (!archivo.exists()) {
            datos = new UsuariosWrapper();
            crearAdminPorDefecto();
            return;
        }
        try {
            JAXBContext contexto = JAXBContext.newInstance(UsuariosWrapper.class);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            datos = (UsuariosWrapper) unmarshaller.unmarshal(archivo);
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo leer " + ARCHIVO, e);
        }
    }

    private void crearAdminPorDefecto() {
        // Para que siempre exista un usuario con el que entrar la primera vez
        Usuario admin = new Usuario("admin", "admin", Rol.ADMINISTRADOR);
        datos.getUsuarios().add(admin);
        guardarEnArchivo();
    }

    private void guardarEnArchivo() {
        try {
            JAXBContext contexto = JAXBContext.newInstance(UsuariosWrapper.class);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(datos, RutaArchivos.get(ARCHIVO));
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo guardar " + ARCHIVO, e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        return datos.getUsuarios();
    }

    @Override
    public Usuario buscarPorId(String id) {
        return datos.getUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void guardar(Usuario usuario) {
        datos.getUsuarios().removeIf(u -> u.getId().equals(usuario.getId()));
        datos.getUsuarios().add(usuario);
        guardarEnArchivo();
    }

    @Override
    public void eliminar(String id) {
        datos.getUsuarios().removeIf(u -> u.getId().equals(id));
        guardarEnArchivo();
    }
}