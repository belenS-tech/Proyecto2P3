package controller;

import view.CambiarClaveView;
import view.LoginView;
import api.AuthService;
import service.SessionManager;
import view.*;
import model.Usuario;
import model.Rol;
import api.CategoriaService;
import api.FuncionarioService;

/**
 * Controlador de la ventana principal (NO es un punto de entrada, no tiene main()).
 * Maneja las acciones de sesión: cambiar clave, cerrar sesión y salir.
 */
public class MainController {

    private final MainView vista;
    private final AuthService authService;
    private final AlertasController alertas;

    public MainController(MainView vista, AuthService authService, Usuario usuario,
                          FuncionarioService funcionarios, CategoriaService categorias) {
        this.vista = vista;
        this.authService = authService;
        this.alertas = new AlertasController(vista.getPanelAlertas(), client.Services.connection());
        vista.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { alertas.close(); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { alertas.close(); }
        });
        try {
            agregarPestanasSegunRol(usuario, funcionarios, categorias);
            registrarEventos();
        } catch (RuntimeException e) {
            alertas.close();
            vista.dispose();
            throw e;
        }
    }

    private void agregarPestanasSegunRol(Usuario usuario,
                                         FuncionarioService funcionarioService,
                                         CategoriaService categoriaService) {
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            FuncionariosView vistaFuncionarios = new FuncionariosView();
            new FuncionariosController(vistaFuncionarios, funcionarioService);
            vista.agregarPestana("Funcionarios", vistaFuncionarios);

            CategoriasView vistaCategorias = new CategoriasView();
            new CategoriasController(vistaCategorias, categoriaService);
            vista.agregarPestana("Categorías", vistaCategorias);

            RecursoView vistaRecursos = new RecursoView();
            new RecursoController(vistaRecursos, categoriaService);
            vista.agregarPestana("Recursos", vistaRecursos);
        }

        if (usuario.getRol() == Rol.FUNCIONARIO) {
            ReservaView vistaReservas = new ReservaView();
            new ReservaController(vistaReservas, categoriaService);
            vista.agregarPestana("Reservas", vistaReservas);
        }

        CalendarizacionRecursosPanel panelCalendarizacion = new CalendarizacionRecursosPanel();
        new controller.CalendarizacionRecursosController(panelCalendarizacion, categoriaService,
                client.Services.get(api.CalendarizacionRecursosService.class));
        vista.agregarPestana("Calendarización", panelCalendarizacion);

        ProgramacionActividadesPanel panelActividades = new ProgramacionActividadesPanel();
        new controller.ProgramacionActividadesController(panelActividades,
                client.Services.get(api.ProgramacionActividadesService.class));
        vista.agregarPestana("Actividades", panelActividades);

        EstadisticasRecursosPanel panelEstadisticasRecursos = new EstadisticasRecursosPanel();
        new controller.EstadisticasRecursosController(panelEstadisticasRecursos,
                client.Services.get(api.EstadisticasRecursosService.class));
        EstadisticasActividadesPanel panelEstadisticasActividades = new EstadisticasActividadesPanel();
        new controller.EstadisticasActividadesController(panelEstadisticasActividades,
                client.Services.get(api.EstadisticasActividadesService.class));
        EstadisticasPanel panelEstadisticas = new EstadisticasPanel(panelEstadisticasRecursos, panelEstadisticasActividades);
        vista.agregarPestana("Estadísticas", panelEstadisticas);
    }


    private void registrarEventos() {
        vista.getItemCambiarClave().addActionListener(e -> abrirCambiarClave());
        vista.getItemCerrarSesion().addActionListener(e -> cerrarSesion());
        vista.getItemSalir().addActionListener(e -> salir());
    }

    private void abrirCambiarClave() {
        CambiarClaveView vistaClave = new CambiarClaveView();
        new CambiarClaveController(vistaClave, authService);
        vistaClave.setVisible(true);
    }

    private void cerrarSesion() {
        if (!vista.confirmar("¿Desea cerrar la sesión?")) {
            return;
        }

        alertas.close();
        try { client.Services.connection().logout(); }
        catch (IllegalArgumentException e) { client.Services.disconnect(); }
        SessionManager.getInstancia().cerrarSesion();
        vista.dispose();

        LoginView loginView = new LoginView();
        try { new LoginController(loginView, client.Services.get(AuthService.class)); }
        catch (IllegalArgumentException e) {
            loginView.mostrarError(e.getMessage()); loginView.dispose(); return;
        }
        loginView.setVisible(true);
    }

    private void salir() {
        if (vista.confirmar("¿Está seguro que desea salir del sistema?")) {
            alertas.close();
            System.exit(0);
        }
    }
}
