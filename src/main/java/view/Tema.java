package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Tema {

    public static final Color FONDO        = new Color(237, 233, 254); // lila clarito
    public static final Color PANEL        = new Color(245, 243, 255); // lila muy suave
    public static final Color ACENTO       = new Color(109, 40, 217);  // violeta oscuro
    public static final Color TEXTO        = new Color(45, 25, 90);    // morado oscuro
    public static final Color TABLA_HEADER = new Color(196, 181, 253); // lila medio
    public static final Color TABLA_SEL    = new Color(221, 214, 254); // lila selección
    public static final Color BORDE        = new Color(167, 139, 250); // lila borde

    /** Aplica el tema a un JPanel y todos sus componentes hijos recursivamente. */
    public static void aplicar(JPanel panel) {
        aplicarComponente(panel);
    }

    private static void aplicarComponente(Component c) {
        if (c instanceof JPanel panel) {
            panel.setBackground(FONDO);
            panel.setForeground(TEXTO);
            if (panel.getBorder() instanceof TitledBorder tb) {
                tb.setTitleColor(ACENTO);
                tb.setBorder(BorderFactory.createLineBorder(BORDE));
            }
            for (Component hijo : panel.getComponents()) {
                aplicarComponente(hijo);
            }
        } else if (c instanceof JLabel lbl) {
            lbl.setForeground(TEXTO);
        } else if (c instanceof JTextField tf) {
            tf.setBackground(Color.WHITE);
            tf.setForeground(TEXTO);
            tf.setCaretColor(ACENTO);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        } else if (c instanceof JTextArea ta) {
            ta.setBackground(Color.WHITE);
            ta.setForeground(TEXTO);
            ta.setCaretColor(ACENTO);
        } else if (c instanceof JComboBox<?> cb) {
            cb.setBackground(Color.WHITE);
            cb.setForeground(TEXTO);
        } else if (c instanceof JSpinner sp) {
            sp.setBackground(Color.WHITE);
            sp.getEditor().setBackground(Color.WHITE);
        } else if (c instanceof JButton btn) {
            btn.setBackground(ACENTO);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACENTO.darker()),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        } else if (c instanceof JScrollPane sp) {
            sp.setBackground(FONDO);
            sp.getViewport().setBackground(FONDO);
            if (sp.getBorder() instanceof TitledBorder tb) {
                tb.setTitleColor(ACENTO);
                tb.setBorder(BorderFactory.createLineBorder(BORDE));
            }
            aplicarComponente(sp.getViewport().getView());
        } else if (c instanceof JTable tabla) {
            tabla.setBackground(Color.WHITE);
            tabla.setForeground(TEXTO);
            tabla.setGridColor(BORDE);
            tabla.setSelectionBackground(TABLA_SEL);
            tabla.setSelectionForeground(TEXTO);
            tabla.setRowHeight(24);
            JTableHeader header = tabla.getTableHeader();
            header.setBackground(TABLA_HEADER);
            header.setForeground(TEXTO);
            header.setFont(header.getFont().deriveFont(Font.BOLD));
        } else if (c instanceof JList<?> list) {
            list.setBackground(Color.WHITE);
            list.setForeground(TEXTO);
            list.setSelectionBackground(TABLA_SEL);
            list.setSelectionForeground(TEXTO);
        } else if (c instanceof Container cont) {
            for (Component hijo : cont.getComponents()) {
                aplicarComponente(hijo);
            }
        }
    }
}
