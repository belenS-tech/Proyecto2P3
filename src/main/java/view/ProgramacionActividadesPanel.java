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

/** Presentación Swing; los eventos y servicios pertenecen a ProgramacionActividadesController. */
public class ProgramacionActividadesPanel extends PanelReporte {
    private static final DateTimeFormatter FORMATO_ENCABEZADO_DIA = DateTimeFormatter.ofPattern("EEE dd/MM");
    private final JSpinner fechaReferenciaSpinner;
    private final JButton btnCargar;
    private final JButton btnImprimir;
    private final JLabel lblSemana;
    private final JLabel lblMensaje;
    private final JTable tablaMatriz;
    private final DefaultTableModel modeloTabla;

    public ProgramacionActividadesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Programación Semanal de Actividades");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.add(titulo, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fechaReferenciaSpinner = new JSpinner(new SpinnerDateModel());
        fechaReferenciaSpinner.setEditor(new JSpinner.DateEditor(fechaReferenciaSpinner, "dd/MM/yyyy"));
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

        panelFiltros.add(new JLabel("Fecha de referencia:"));
        panelFiltros.add(fechaReferenciaSpinner);
        panelFiltros.add(btnCargar);
        panelFiltros.add(btnImprimir);

        lblSemana = new JLabel(" ");
        lblSemana.setFont(lblSemana.getFont().deriveFont(Font.BOLD));

        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        panelInfo.add(panelFiltros);
        panelInfo.add(lblSemana);
        panelSuperior.add(panelInfo, BorderLayout.CENTER);

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

    public void mostrarResultado(ResultadoProgramacionActividades resultado) {
        modeloTabla.setColumnCount(0);
        modeloTabla.addColumn("Hora");
        for (LocalDate dia : resultado.getDias()) {
            modeloTabla.addColumn(dia.format(FORMATO_ENCABEZADO_DIA));
        }

        modeloTabla.setRowCount(0);
        for (FilaActividad fila : resultado.getFilas()) {
            List<Object> valores = new ArrayList<>();
            valores.add(fila.getHoraTexto());
            for (LocalDate dia : resultado.getDias()) {
                valores.add(fila.getValor(dia));
            }
            modeloTabla.addRow(valores.toArray());
        }
    }
    public JSpinner getFechaReferenciaSpinner() { return fechaReferenciaSpinner; }
    public JButton getBtnCargar() { return btnCargar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblSemana() { return lblSemana; }
    public JLabel getLblMensaje() { return lblMensaje; }
    public JTable getTablaMatriz() { return tablaMatriz; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
}
