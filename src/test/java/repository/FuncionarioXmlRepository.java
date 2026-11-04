package repository;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.Funcionario;
import model.FuncionariosWrapper;

import util.RutaArchivos;
import java.io.File;
import java.util.List;

public class FuncionarioXmlRepository implements FuncionarioRepository {

    private static final String ARCHIVO = "src/main/resources/data/funcionarios.xml";
    private FuncionariosWrapper datos;

    public FuncionarioXmlRepository() {
        cargar();
    }

    private void cargar() {
        File archivo = RutaArchivos.get(ARCHIVO);
        if (!archivo.exists()) {
            datos = new FuncionariosWrapper();
            guardarEnArchivo();
            return;
        }
        try {
            JAXBContext contexto = JAXBContext.newInstance(FuncionariosWrapper.class);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            datos = (FuncionariosWrapper) unmarshaller.unmarshal(archivo);
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo leer " + ARCHIVO, e);
        }
    }

    private void guardarEnArchivo() {
        try {
            JAXBContext contexto = JAXBContext.newInstance(FuncionariosWrapper.class);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(datos, RutaArchivos.get(ARCHIVO));
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo guardar " + ARCHIVO, e);
        }
    }

    @Override
    public List<Funcionario> listarTodos() {
        return datos.getFuncionarios();
    }

    @Override
    public Funcionario buscarPorId(String id) {
        return datos.getFuncionarios().stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void guardar(Funcionario funcionario) {
        eliminarSinPersistir(funcionario.getId());
        datos.getFuncionarios().add(funcionario);
        guardarEnArchivo();
    }

    @Override
    public void eliminar(String id) {
        eliminarSinPersistir(id);
        guardarEnArchivo();
    }

    private void eliminarSinPersistir(String id) {
        datos.getFuncionarios().removeIf(f -> f.getId().equals(id));
    }
}