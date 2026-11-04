package repository;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.CategoriaRecurso;
import model.CategoriasWrapper;

import util.RutaArchivos;
import java.io.File;
import java.util.List;

public class CategoriaXmlRepository implements CategoriaRepository {

    private static final String ARCHIVO = "src/main/resources/data/categorias.xml";
    private CategoriasWrapper datos;

    public CategoriaXmlRepository() {
        cargar();
    }

    private void cargar() {
        File archivo = RutaArchivos.get(ARCHIVO);
        if (!archivo.exists()) {
            datos = new CategoriasWrapper();
            guardarEnArchivo();
            return;
        }
        try {
            JAXBContext contexto = JAXBContext.newInstance(CategoriasWrapper.class);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            datos = (CategoriasWrapper) unmarshaller.unmarshal(archivo);
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo leer " + ARCHIVO, e);
        }
    }

    private void guardarEnArchivo() {
        try {
            JAXBContext contexto = JAXBContext.newInstance(CategoriasWrapper.class);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(datos, RutaArchivos.get(ARCHIVO));
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo guardar " + ARCHIVO, e);
        }
    }

    @Override
    public List<CategoriaRecurso> listarTodas() {
        return listarTodos();
    }

    @Override
    public List<CategoriaRecurso> listarTodos() {
        return datos.getCategorias();
    }

    @Override
    public CategoriaRecurso buscarPorId(String id) {
        return datos.getCategorias().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {
        eliminarSinPersistir(categoria.getId());
        datos.getCategorias().add(categoria);
        guardarEnArchivo();
    }

    @Override
    public void eliminar(String id) {
        eliminarSinPersistir(id);
        guardarEnArchivo();
    }

    private void eliminarSinPersistir(String id) {
        datos.getCategorias().removeIf(c -> c.getId().equals(id));
    }
}