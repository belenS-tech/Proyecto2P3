package view;

import model.Reserva;
import network.Protocol.Alert;
import javax.swing.*;
import java.awt.*;

/** Vista informativa de la sesión: no elimina ni modifica reservas del servidor. */
public class AlertasPanel extends JPanel {
    private final DefaultListModel<Alert> model = new DefaultListModel<>();
    private final JList<Alert> list = new JList<>(model);
    private final JLabel status = new JLabel("Sin reservas nuevas en esta sesión.");
    private final JButton view = new JButton("Ver alerta");
    private final JButton discard = new JButton("Descartar");

    public AlertasPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(285, 400));
        JLabel title = new JLabel("Alertas de reservas"); title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 8)); heading.add(title); heading.add(status);
        add(heading, BorderLayout.NORTH);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getAccessibleContext().setAccessibleName("Reservas nuevas de otros usuarios");
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
                Alert a = (Alert) value;
                JLabel label = (JLabel) super.getListCellRendererComponent(l,
                        a.reserva().getId() + " · " + a.author(), index, selected, focus);
                label.setToolTipText(a.reserva().getActividad()); label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
                return label;
            }
        });
        add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 6)); actions.add(view); actions.add(discard);
        add(actions, BorderLayout.SOUTH);
        view.setToolTipText("Ver el detalle de la reserva seleccionada");
        discard.setToolTipText("Quitar la alerta de esta lista");
        view.setEnabled(false); discard.setEnabled(false);
        list.addListSelectionListener(e -> { boolean selected = list.getSelectedIndex() >= 0; view.setEnabled(selected); discard.setEnabled(selected); });
        Tema.aplicar(this);
        view.setFocusPainted(true); discard.setFocusPainted(true);
        setIcon(view, "/icons/consultar.png"); setIcon(discard, "/icons/eliminar.png");
    }
    private void setIcon(JButton button, String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) button.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(16,16,Image.SCALE_SMOOTH)));
    }
    public void agregar(Alert alert) {
        model.addElement(alert); status.setText(model.size() + " alerta(s) en esta sesión");
    }
    public Alert seleccionada() { return list.getSelectedValue(); }
    public JButton ver() { return view; }
    public JButton descartar() { return discard; }
    public boolean confirmarDescarte() {
        return JOptionPane.showConfirmDialog(this,
                "¿Desea descartar esta alerta? La reserva se conservará.", "Descartar alerta",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    public void quitarSeleccionada() {
        int index = list.getSelectedIndex(); if (index < 0) return;
        model.remove(index);
        if (!model.isEmpty()) list.setSelectedIndex(Math.min(index, model.size() - 1));
        status.setText(model.isEmpty() ? "Sin reservas nuevas en esta sesión." : model.size() + " alerta(s) en esta sesión");
    }
    public void mostrarDetalle(Reserva r) {
        StringBuilder text = new StringBuilder("Reserva: " + r.getId() + "\nActividad: " + r.getActividad()
                + "\nFuncionario: " + r.getFuncionarioId() + "\nFecha: " + r.getFecha()
                + "\nHorario: " + r.getHoraInicio() + " - " + r.getHoraFin() + "\nEstado: " + r.getEstado() + "\nRecursos:\n");
        r.getDetalles().forEach(d -> text.append("• ").append(d.getCategoria().getDescripcion()).append(": ")
                .append(d.getRecurso().getDescripcion()).append('\n'));
        JTextArea detail = new JTextArea(text.toString(), 14, 35);
        detail.setEditable(false); detail.setLineWrap(true); detail.setWrapStyleWord(true); detail.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(detail), "Detalle de alerta", JOptionPane.INFORMATION_MESSAGE);
    }
}
