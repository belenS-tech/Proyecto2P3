package controller;

import model.Funcionario;
import model.Usuario;
import report.PdfReportService;
import report.ReportHeader;
import report.ReportTable;
import api.FuncionarioService;
import service.SessionManager;
import view.FuncionariosView;

import javax.swing.event.ListSelectionEvent;
import java.util.List;

public class FuncionariosController {

    private final FuncionariosView vista;
    private final FuncionarioService servicio;

    public FuncionariosController(FuncionariosView vista, FuncionarioService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        registrarEventos();
        cargarListado();
    }

    private void registrarEventos() {
        vista.getBotonBuscar().addActionListener(e -> buscar());
        vista.getBotonGuardar().addActionListener(e -> guardar());
        vista.getBotonBorrar().addActionListener(e -> borrar());
        vista.getBotonLimpiar().addActionListener(e -> vista.limpiarFormulario());
        vista.getBotonImprimir().addActionListener(e -> imprimir());

        // Al seleccionar una fila, se cargan sus datos en el formulario
        vista.getTabla().getSelectionModel().addListSelectionListener(this::alSeleccionarFila);
    }

    private void alSeleccionarFila(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        String id = vista.getIdSeleccionado();
        if (id == null) {
            return;
        }
        Funcionario funcionario = servicio.buscarPorId(id);
        if (funcionario != null) {
            vista.mostrarEnFormulario(funcionario);
        }
    }

    private void cargarListado() {
        vista.cargarTabla(servicio.listarTodos());
    }

    private void buscar() {
        vista.cargarTabla(servicio.buscar(vista.getBuscarId(), vista.getBuscarNombre()));
    }

    /**
     Si el ID ya existe se modifica; si no, se incluye como nuevo.
     */
    private void guardar() {
        try {
            String id = vista.getId();

            if (id != null && !id.isBlank() && servicio.buscarPorId(id.trim()) != null) {
                servicio.modificar(id, vista.getNombre(), vista.getTelefono());
                vista.mostrarMensaje("Funcionario modificado correctamente.");
            } else {
                servicio.incluir(id, vista.getNombre(), vista.getTelefono());
                vista.mostrarMensaje("Funcionario incluido. Su clave inicial es igual al ID.");
            }

            vista.limpiarFormulario();
            cargarListado();

        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void borrar() {
        try {
            String id = vista.getId();
            if (id == null || id.isBlank()) {
                vista.mostrarError("Seleccione un funcionario del listado.");
                return;
            }

            if (!vista.confirmar("¿Desea borrar el funcionario " + id + " y su usuario?")) {
                return;
            }

            servicio.eliminar(id);
            vista.mostrarMensaje("Funcionario borrado correctamente.");
            vista.limpiarFormulario();
            cargarListado();

        } catch (RuntimeException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    /** Genera el reporte PDF respetando los filtros de búsqueda aplicados. */
    private void imprimir() {
        try {
            List<Funcionario> listado = servicio.buscar(vista.getBuscarId(), vista.getBuscarNombre());

            ReportTable tabla = new ReportTable(List.of("Id", "Nombre", "Teléfono"));
            for (Funcionario f : listado)
                tabla.agregarFila(f.getId(), f.getNombre(), f.getTelefono());

            Usuario usuarioActivo = SessionManager.getInstancia().getUsuarioActual();
            String nombreUsuario = usuarioActivo != null ? usuarioActivo.getId() : "";
            ReportHeader header = new ReportHeader("Listado de Funcionarios", nombreUsuario, describirFiltros());

            String ruta = vista.elegirRutaReporte("funcionarios.pdf");
            if (ruta == null) return;

            new PdfReportService().generarReporteTabla(ruta, header, tabla);
            vista.mostrarMensaje("Reporte generado: " + ruta);

        } catch (Exception ex) {
            vista.mostrarError("No se pudo generar el reporte: " + ex.getMessage());
        }
    }

    /** Describe los filtros aplicados para que queden registrados en el PDF. */
    private String describirFiltros() {
        String id = vista.getBuscarId();
        String nombre = vista.getBuscarNombre();

        StringBuilder filtros = new StringBuilder();
        if (id != null && !id.isBlank()) {
            filtros.append("ID: ").append(id.trim());
        }
        if (nombre != null && !nombre.isBlank()) {
            if (filtros.length() > 0) {
                filtros.append(", ");
            }
            filtros.append("Nombre: ").append(nombre.trim());
        }
        return filtros.toString();
    }
}