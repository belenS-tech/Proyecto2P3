package view;

import model.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del sistema (No es un punto de entrada, no tiene main()).
 * Contiene las pestañas de los módulos y el menú de sesión.
 * Las pestañas se agregan según el rol del usuario autenticado.
 * Juan.
 */
public class MainView extends JFrame {

    private JTabbedPane pestanas;

    private JMenuItem itemCambiarClave;
    private JMenuItem itemCerrarSesion;
    private JMenuItem itemSalir;
    private AlertasPanel panelAlertas;

    public MainView(Usuario usuarioActivo) {
        super("Sistema de Reserva de Recursos");
        construirVentana(usuarioActivo);
    }

    private void construirVentana(Usuario usuario) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setTitle("Sistema de Reserva de Recursos  —  " + usuario.getId() + " (" + usuario.getRol() + ")");

        // Fondo general
        getContentPane().setBackground(new Color(30, 30, 45));

        setJMenuBar(construirMenu());

        pestanas = new JTabbedPane();
        pestanas.setBackground(new Color(45, 45, 65));
        pestanas.setForeground(new Color(200, 200, 255));
        pestanas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pestanas.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panelAlertas = new AlertasPanel();
        JPanel content = new JPanel(new BorderLayout());
        content.add(pestanas, BorderLayout.CENTER);
        content.add(panelAlertas, BorderLayout.EAST);
        setContentPane(content);
    }

    private JMenuBar construirMenu() {
        JMenuBar barra = new JMenuBar();
        barra.setBackground(new Color(45, 45, 65));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(99, 102, 241)));

        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.setForeground(new Color(200, 200, 255));
        menuSistema.setFont(new Font("Segoe UI", Font.BOLD, 13));

        itemCambiarClave = new JMenuItem("Cambiar clave");
        itemCerrarSesion = new JMenuItem("Cerrar sesión");
        itemSalir = new JMenuItem("Salir");

        for (JMenuItem item : new JMenuItem[]{itemCambiarClave, itemCerrarSesion, itemSalir}) {
            item.setBackground(new Color(45, 45, 65));
            item.setForeground(new Color(200, 200, 255));
            item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        menuSistema.add(itemCambiarClave);
        menuSistema.addSeparator();
        menuSistema.add(itemCerrarSesion);
        menuSistema.add(itemSalir);

        barra.add(menuSistema);
        return barra;
    }

    /**
     * Punto de conexión para el resto del equipo:
     * cada integrante entrega el JPanel de su pantalla y se agrega aquí.
     */
    public void agregarPestana(String titulo, JPanel panel) {
        pestanas.addTab(titulo, panel);
    }

    /** Panel temporal mientras la pantalla real no exista. */
    private JPanel pendiente(String nombre) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(nombre + ": pendiente."));
        return panel;
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }

    public boolean confirmar(String mensaje) {
        int respuesta = JOptionPane.showConfirmDialog(
                this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION);
        return respuesta == JOptionPane.YES_OPTION;
    }

    public JMenuItem getItemCambiarClave() {
        return itemCambiarClave;
    }

    public JMenuItem getItemCerrarSesion() {
        return itemCerrarSesion;
    }

    public JMenuItem getItemSalir() {
        return itemSalir;
    }
    public AlertasPanel getPanelAlertas() { return panelAlertas; }
}
