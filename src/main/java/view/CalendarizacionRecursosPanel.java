package view;

import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/** Presentación Swing; los eventos y servicios pertenecen a CalendarizacionRecursosController. */
public class CalendarizacionRecursosPanel extends PanelReporte {
    private final JSpinner fechaSpinner;
    private final JComboBox<CategoriaRecurso> categoriaCombo;
    private final JButton btnCargar;
    private final JButton btnImprimir;
    private final JLabel lblMensaje;
    private final JTable tablaMatriz;
    private final DefaultTableModel modeloTabla;

    public CalendarizacionRecursosPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Calendarización de Recursos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.add(titulo, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fechaSpinner = new JSpinner(new SpinnerDateModel());
        fechaSpinner.setEditor(new JSpinner.DateEditor(fechaSpinner, "dd/MM/yyyy"));
        categoriaCombo = new JComboBox<>();
        btnCargar = new JButton("Cargar");
        btnImprimir = new JButton("Imprimir");
        btnImprimir.setEnabled(false);

        URL urlImprimir = getClass().getResource("/icons/imprimir.png");
        if (urlImprimir != null) {
            ImageIcon original = new ImageIcon(urlImprimir);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnImprimir.setIcon(new ImageIcon(img));
        }

        URL urlCargar = getClass().getResource("/icons/cargar.png");
        if (urlCargar != null) {
            ImageIcon original = new ImageIcon(urlCargar);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnCargar.setIcon(new ImageIcon(img));
        }

        panelFiltros.add(new JLabel("Fecha:"));
        panelFiltros.add(fechaSpinner);
        panelFiltros.add(new JLabel("Categoría:"));
        panelFiltros.add(categoriaCombo);
        panelFiltros.add(btnCargar);
        panelFiltros.add(btnImprimir);
        panelSuperior.add(panelFiltros, BorderLayout.CENTER);

        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(new Color(180, 0, 0));

        modeloTabla = new DefaultTableModel() {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        modeloTabla.addColumn("Hora");
        tablaMatriz = new JTable(modeloTabla);
        tablaMatriz.setRowHeight(28);

        add(panelSuperior, BorderLayout.NORTH);
        add(lblMensaje, BorderLayout.SOUTH);
        add(new JScrollPane(tablaMatriz), BorderLayout.CENTER);
        Tema.aplicar(this);
    }

    public void mostrarResultado(ResultadoCalendarizacion resultado) {
        modeloTabla.setColumnCount(0);
        modeloTabla.addColumn("Hora");
        for (Recurso recurso : resultado.getRecursos()) {
            modeloTabla.addColumn(recurso.getDescripcion());
        }

        modeloTabla.setRowCount(0);
        for (FilaCalendarizacion fila : resultado.getFilas()) {
            List<Object> valores = new ArrayList<>();
            valores.add(fila.getHoraTexto());
            for (Recurso recurso : resultado.getRecursos()) {
                valores.add(fila.getValor(recurso.getId()));
            }
            modeloTabla.addRow(valores.toArray());
        }
    }

    public void limpiarTabla() {
        modeloTabla.setColumnCount(0);
        modeloTabla.addColumn("Hora");
        modeloTabla.setRowCount(0);
    }
    public JSpinner getFechaSpinner() { return fechaSpinner; }
    public JComboBox<CategoriaRecurso> getCategoriaCombo() { return categoriaCombo; }
    public JButton getBtnCargar() { return btnCargar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblMensaje() { return lblMensaje; }
    public JTable getTablaMatriz() { return tablaMatriz; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
}
