package repository;

import model.Recurso;
import model.CategoriaRecurso;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import util.RutaArchivos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecursoXmlRepository implements RecursoRepository {

    private File archivoFile() { return RutaArchivos.get("src/main/resources/data/recursos.xml"); }

    @Override
    public void guardar(Recurso recurso) {
        try {
            File file = archivoFile();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(file);

            Element raiz = document.getDocumentElement(); //raiz -> <recursos>

            Element elementoRecurso = document.createElement("recurso");

            Element elementoId = document.createElement("id");
            elementoId.setTextContent(String.valueOf(recurso.getId()));
            elementoRecurso.appendChild(elementoId);

            Element elementoCategoria = document.createElement("categoria");
            elementoCategoria.setTextContent(recurso.getCategoria().getId());
            elementoRecurso.appendChild(elementoCategoria);

            Element elementoDescripcion = document.createElement("descripcion");
            elementoDescripcion.setTextContent(recurso.getDescripcion());
            elementoRecurso.appendChild(elementoDescripcion);

            raiz.appendChild(elementoRecurso);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(archivoFile());
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Recurso> listar() {
        try {
            File file = archivoFile();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList nodeList = document.getElementsByTagName("recurso");
            List<Recurso> recursos = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element elementoRecurso = (Element) nodeList.item(i);
                String id = elementoRecurso.getElementsByTagName("id").item(0).getTextContent();
                String categoriaId = elementoRecurso.getElementsByTagName("categoria").item(0).getTextContent();
                String descripcion = elementoRecurso.getElementsByTagName("descripcion").item(0).getTextContent();
                CategoriaRecurso cat = new CategoriaXmlRepository().buscarPorId(categoriaId);
                if (cat == null) cat = new CategoriaRecurso(categoriaId, categoriaId);
                Recurso recurso = new Recurso(id, cat, descripcion);
                recursos.add(recurso);
                System.out.println("Recurso ID: " + id + ", Categoria: " + categoriaId + ", Descripcion: " + descripcion);
            }
            return recursos;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Recurso buscarPorId(String id) {
        List<Recurso> recursos = listar();
        for (Recurso recurso : recursos) {
            if (recurso.getId().equals(id)) {
                return recurso;
            }
        }
        return null;
    }

    @Override
    public void actualizar(Recurso recurso) {
        try {
            File file = archivoFile();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList nodeList = document.getElementsByTagName("recurso");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element elementoRecurso = (Element) nodeList.item(i);
                String idRecurso = elementoRecurso.getElementsByTagName("id").item(0).getTextContent();
                if (idRecurso.equals(recurso.getId())) {
                    elementoRecurso.getElementsByTagName("categoria").item(0).setTextContent(recurso.getCategoria().getId());
                    elementoRecurso.getElementsByTagName("descripcion").item(0).setTextContent(recurso.getDescripcion());
                    break;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(archivoFile());
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eliminar(Recurso recurso) {
        try {
            File file = archivoFile();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList nodeList = document.getElementsByTagName("recurso");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element elementoRecurso = (Element) nodeList.item(i);
                String idRecurso = elementoRecurso.getElementsByTagName("id").item(0).getTextContent();
                if (idRecurso.equals(recurso.getId())) {
                    elementoRecurso.getParentNode().removeChild(elementoRecurso);
                    break;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(archivoFile());
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Recurso> buscarPorCategoria(String categoriaId) {
        return listar().stream()
                .filter(recurso -> recurso.getCategoria() != null
                        && categoriaId != null
                        && categoriaId.equals(recurso.getCategoria().getId()))
                .toList();
    }





    @Override
    public List<Recurso> listarTodos() {
        return listar();
    }
}