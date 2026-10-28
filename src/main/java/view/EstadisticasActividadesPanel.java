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

/** Presentación Swing; los eventos y servicios pertenecen a EstadisticasActividadesController. */
public class EstadisticasActividadesPanel extends PanelReporte {
    private final JSpinner fechaDesdeSpinner;
    private final JSpinner fechaHastaSpinner;
    private final JButton btnCalcular;
    private final JButton btnImprimir;
    private final JLabel lblMensaje;
    private final JTable tablaEstadisticas;
    private final DefaultTableModel modeloTabla;
    private final DefaultCategoryDataset dataset;
    private final JFreeChart grafico;
    private final ChartPanel panelGrafico;

    public EstadisticasActividadesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Estadísticas de Actividades");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fechaDesdeSpinner = new JSpinner(new SpinnerDateModel());
        fechaDesdeSpinner.setEditor(new JSpinner.DateEditor(fechaDesdeSpinner, "dd/MM/yyyy"));
        fechaHastaSpinner = new JSpinner(new SpinnerDateModel());
        fechaHastaSpinner.setEditor(new JSpinner.DateEditor(fechaHastaSpinner, "dd/MM/yyyy"));
        btnCalcular = new JButton("Calcular");
        btnImprimir = new JButton("Imprimir");
        btnImprimir.setEnabled(false);

        URL urlImprimir = getClass().getResource("/icons/imprimir.png");
        if (urlImprimir != null) {
            ImageIcon original = new ImageIcon(urlImprimir);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnImprimir.setIcon(new ImageIcon(img));
        }

        URL urlCalcular = getClass().getResource("/icons/cargar.png");
        if (urlCalcular != null) {
            ImageIcon original = new ImageIcon(urlCalcular);
            Image img = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnCalcular.setIcon(new ImageIcon(img));
        }

        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(fechaDesdeSpinner);
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(fechaHastaSpinner);
        panelFiltros.add(btnCalcular);
        panelFiltros.add(btnImprimir);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(titulo, BorderLayout.NORTH);
        panelSuperior.add(panelFiltros, BorderLayout.CENTER);

        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(new Color(222, 137, 253, 187));

        modeloTabla = new DefaultTableModel(new Object[]{"Semana", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tablaEstadisticas = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaEstadisticas);
        scrollTabla.setPreferredSize(new Dimension(400, 160));

        dataset = new DefaultCategoryDataset();
        grafico = ChartFactory.createBarChart(
                "Actividades por semana", "Semana", "Cantidad",
                dataset, PlotOrientation.VERTICAL, false, true, false);
        org.jfree.chart.plot.CategoryPlot plot = grafico.getCategoryPlot();
        ((org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer())
                .setSeriesPaint(0, new Color(167, 139, 250));
        panelGrafico = new ChartPanel(grafico);
        panelGrafico.setPreferredSize(new Dimension(500, 320));

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.add(scrollTabla, BorderLayout.WEST);
        panelCentro.add(panelGrafico, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(lblMensaje, BorderLayout.SOUTH);
        add(panelCentro, BorderLayout.CENTER);
        Tema.aplicar(this);
    }


    public void mostrarResultado(Map<String, Integer> resultado) {
        modeloTabla.setRowCount(0);
        dataset.clear();
        for (Map.Entry<String, Integer> entrada : resultado.entrySet()) {
            modeloTabla.addRow(new Object[]{entrada.getKey(), entrada.getValue()});
            dataset.addValue(entrada.getValue(), "Actividades", entrada.getKey());
        }
    }

    public byte[] capturarGraficoComoPng() throws Exception {
        BufferedImage imagen = grafico.createBufferedImage(480, 320);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", buffer);
        return buffer.toByteArray();
    }
    public JSpinner getFechaDesdeSpinner() { return fechaDesdeSpinner; }
    public JSpinner getFechaHastaSpinner() { return fechaHastaSpinner; }
    public JButton getBtnCalcular() { return btnCalcular; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblMensaje() { return lblMensaje; }
    public JTable getTablaEstadisticas() { return tablaEstadisticas; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public DefaultCategoryDataset getDataset() { return dataset; }
    public JFreeChart getGrafico() { return grafico; }
    public ChartPanel getPanelGrafico() { return panelGrafico; }
}
