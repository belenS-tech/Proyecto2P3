package repository;

import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReservaXmlRepository implements ReservaRepository {

    private final String archivo = "src/main/resources/data/reservas.xml";
    private File archivoFile() { return RutaArchivos.get(archivo); }
    private final RecursoRepository recursoRepository;

    public ReservaXmlRepository() {
        this(new RecursoXmlRepository());
    }

    public ReservaXmlRepository(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Override
    public void guardar(Reserva reserva) {
        try {
            Document document = cargarDocumento();
            Element raiz = document.getDocumentElement();

            Element elementoReserva = document.createElement("reserva");
            appendTextElement(document, elementoReserva, "id", reserva.getId());
            appendTextElement(document, elementoReserva, "actividad", reserva.getActividad());
            appendTextElement(document, elementoReserva, "fecha", reserva.getFecha().toString());
            appendTextElement(document, elementoReserva, "horaInicio", reserva.getHoraInicio().toString());
            appendTextElement(document, elementoReserva, "horaFin", reserva.getHoraFin().toString());
            appendTextElement(document, elementoReserva, "funcionarioId", reserva.getFuncionarioId());
            appendTextElement(document, elementoReserva, "estado", reserva.getEstado().name());

            Element elementoDetalles = document.createElement("detalles");
            for (DetalleReserva detalle : reserva.getDetalles()) {
                Element elementoDetalle = document.createElement("detalle");
                appendTextElement(document, elementoDetalle, "recursoId", detalle.getRecurso().getId());
                appendTextElement(document, elementoDetalle, "categoria", detalle.getCategoria().getId());
                elementoDetalles.appendChild(elementoDetalle);
            }
            elementoReserva.appendChild(elementoDetalles);
            raiz.appendChild(elementoReserva);

            guardarDocumento(document);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar la reserva", e);
        }
    }

    @Override
    public List<Reserva> listar() {
        List<Reserva> reservas = new ArrayList<>();
        try {
            Document document = cargarDocumento();
            NodeList listaReservas = document.getElementsByTagName("reserva");
            for (int i = 0; i < listaReservas.getLength(); i++) {
                Element elementoReserva = (Element) listaReservas.item(i);
                Reserva reserva = leerReserva(elementoReserva);
                reservas.add(reserva);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron listar las reservas", e);
        }
        return reservas;
    }

    @Override
    public Reserva buscarPorId(String id) {
        for (Reserva reserva : listar()) {
            if (Objects.equals(reserva.getId(), id)) {
                return reserva;
            }
        }
        return null;
    }

    @Override
    public void actualizar(Reserva reserva) {
        try {
            Document document = cargarDocumento();
            NodeList listaReservas = document.getElementsByTagName("reserva");
            for (int i = 0; i < listaReservas.getLength(); i++) {
                Element elementoReserva = (Element) listaReservas.item(i);
                if (Objects.equals(texto(elementoReserva, "id"), reserva.getId())) {
                    elementoReserva.getElementsByTagName("actividad").item(0).setTextContent(reserva.getActividad());
                    elementoReserva.getElementsByTagName("fecha").item(0).setTextContent(reserva.getFecha().toString());
                    elementoReserva.getElementsByTagName("horaInicio").item(0).setTextContent(reserva.getHoraInicio().toString());
                    elementoReserva.getElementsByTagName("horaFin").item(0).setTextContent(reserva.getHoraFin().toString());
                    elementoReserva.getElementsByTagName("funcionarioId").item(0).setTextContent(reserva.getFuncionarioId());
                    elementoReserva.getElementsByTagName("estado").item(0).setTextContent(reserva.getEstado().name());

                    NodeList detallesExistentes = elementoReserva.getElementsByTagName("detalles");
                    if (detallesExistentes.getLength() > 0) {
                        elementoReserva.removeChild(detallesExistentes.item(0));
                    }

                    Element elementoDetalles = document.createElement("detalles");
                    for (DetalleReserva detalle : reserva.getDetalles()) {
                        Element elementoDetalle = document.createElement("detalle");
                        appendTextElement(document, elementoDetalle, "recursoId", detalle.getRecurso().getId());
                        appendTextElement(document, elementoDetalle, "categoria", detalle.getCategoria().getId());
                        elementoDetalles.appendChild(elementoDetalle);
                    }
                    elementoReserva.appendChild(elementoDetalles);
                    guardarDocumento(document);
                    return;
                }
            }
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo actualizar la reserva", e);
        }
    }

    @Override
    public void eliminar(Reserva reserva) {
        try {
            Document document = cargarDocumento();
            NodeList listaReservas = document.getElementsByTagName("reserva");
            for (int i = 0; i < listaReservas.getLength(); i++) {
                Element elementoReserva = (Element) listaReservas.item(i);
                if (Objects.equals(texto(elementoReserva, "id"), reserva.getId())) {
                    elementoReserva.getParentNode().removeChild(elementoReserva);
                    guardarDocumento(document);
                    return;
                }
            }
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar la reserva", e);
        }
    }

    private Reserva leerReserva(Element elementoReserva) {
        String id = texto(elementoReserva, "id");
        String actividad = texto(elementoReserva, "actividad");
        LocalDate fecha = LocalDate.parse(texto(elementoReserva, "fecha"));
        LocalTime horaInicio = LocalTime.parse(texto(elementoReserva, "horaInicio"));
        LocalTime horaFin = LocalTime.parse(texto(elementoReserva, "horaFin"));
        String funcionarioId = texto(elementoReserva, "funcionarioId");
        EstadoReserva estado = EstadoReserva.valueOf(texto(elementoReserva, "estado"));

        Reserva reserva = new Reserva(id, actividad, fecha, horaInicio, horaFin, funcionarioId, estado);

        NodeList nodosDetalles = elementoReserva.getElementsByTagName("detalles");
        if (nodosDetalles.getLength() > 0) {
            Element elementoDetalles = (Element) nodosDetalles.item(0);
            NodeList listaDetalles = elementoDetalles.getElementsByTagName("detalle");
            for (int i = 0; i < listaDetalles.getLength(); i++) {
                Element elementoDetalle = (Element) listaDetalles.item(i);
                String recursoId = texto(elementoDetalle, "recursoId");
                String categoriaId = texto(elementoDetalle, "categoria");
                Recurso recurso = recursoRepository.buscarPorId(recursoId);
                if (recurso != null) {
                    CategoriaRecurso cat = new repository.CategoriaXmlRepository().buscarPorId(categoriaId);
                    if (cat == null) cat = new CategoriaRecurso(categoriaId, categoriaId);
                    reserva.getDetalles().add(new DetalleReserva(cat, recurso));
                }
            }
        }

        return reserva;
    }

    private Document cargarDocumento() throws Exception {
        File file = archivoFile();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document;

        if (file.exists()) {
            document = builder.parse(file);
        } else {
            document = builder.newDocument();
            Element raiz = document.createElement("reservas");
            document.appendChild(raiz);
        }

        if (document.getDocumentElement() == null) {
            Element raiz = document.createElement("reservas");
            document.appendChild(raiz);
        }

        return document;
    }

    private void guardarDocumento(Document document) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(archivoFile());
        transformer.transform(source, result);
    }

    private void appendTextElement(Document document, Element parent, String nombre, String texto) {
        Element elemento = document.createElement(nombre);
        elemento.setTextContent(texto);
        parent.appendChild(elemento);
    }

    private String texto(Element parent, String nombre) {
        return parent.getElementsByTagName(nombre).item(0).getTextContent();
    }
}
