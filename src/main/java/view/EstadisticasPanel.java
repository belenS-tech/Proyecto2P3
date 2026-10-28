package view;


import javax.swing.*;
import java.awt.*;

/**
 * Pantalla general de Estadísticas: une en pestañas la estadística de
 * recursos (JPanel construido por el Integrante 1) y la estadística
 * de actividades (JPanel construido por el Integrante 3).
 *
 * Es un JPanel (no JFrame) porque a su vez debe encajar dentro de la
 * pestaña "Estadísticas" de la ventana principal (MainView).
 */
public class EstadisticasPanel extends JPanel {

    private final JTabbedPane tabbedPane;
    public EstadisticasPanel(EstadisticasRecursosPanel panelRecursos, EstadisticasActividadesPanel panelActividades) {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Estadísticas de Recursos", panelRecursos);
        tabbedPane.addTab("Estadísticas de Actividades", panelActividades);
        add(tabbedPane, BorderLayout.CENTER);
        Tema.aplicar(this);
    }
}
