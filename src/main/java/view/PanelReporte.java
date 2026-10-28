package view;

import javax.swing.*;
import java.awt.Color;

/** Presentación compartida de mensajes y selección de archivo de reportes Swing. */
public abstract class PanelReporte extends JPanel {
    public abstract JLabel getLblMensaje();

    public void mostrarError(String mensaje) {
        getLblMensaje().setForeground(new Color(180, 0, 0));
        getLblMensaje().setText(mensaje);
    }

    public String elegirRutaGuardado(String nombreSugerido) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte PDF");
        selector.setSelectedFile(new java.io.File(nombreSugerido));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        String ruta = selector.getSelectedFile().getAbsolutePath();
        return ruta.toLowerCase().endsWith(".pdf") ? ruta : ruta + ".pdf";
    }
}
