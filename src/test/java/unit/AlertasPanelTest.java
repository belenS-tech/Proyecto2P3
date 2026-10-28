package unit;

import model.*;
import network.Protocol.Alert;
import org.junit.jupiter.api.Test;
import view.AlertasPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class AlertasPanelTest {
    @Test void seleccionYDescarteLocalConservanElObjetoReserva() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AlertasPanel panel = new AlertasPanel();
            assertFalse(panel.ver().isEnabled()); assertFalse(panel.descartar().isEnabled());
            Reserva r = reservation(); panel.agregar(new Alert("otro",r));
            JList<?> list = findList(panel); assertEquals(1,list.getModel().getSize());
            list.setSelectedIndex(0); assertTrue(panel.ver().isEnabled());
            assertEquals(r,panel.seleccionada().reserva());
            panel.quitarSeleccionada(); assertEquals(0,list.getModel().getSize());
            assertFalse(panel.ver().isEnabled()); assertFalse(panel.descartar().isEnabled());
            assertEquals(EstadoReserva.ACTIVA,r.getEstado()); assertEquals("RES-000001",r.getId());
        });
    }
    @Test void renderizaAlertasConListaYLargoDeTextoEnPanelEstrecho() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                AlertasPanel panel = new AlertasPanel();
                for (int i=0;i<20;i++) panel.agregar(new Alert("funcionario-con-identificador-largo",reservation()));
                findList(panel).setSelectedIndex(0);
                panel.setSize(285,600); layout(panel);
                BufferedImage image = new BufferedImage(285,600,BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics(); panel.printAll(g); g.dispose();
                Path file = Path.of("target/ui/alertas.png"); Files.createDirectories(file.getParent());
                javax.imageio.ImageIO.write(image,"png",file.toFile());
                assertTrue(panel.ver().getWidth()>0); assertTrue(panel.descartar().getY()>panel.ver().getY());
            } catch (java.io.IOException e) { throw new RuntimeException(e); }
        });
    }
    static Reserva reservation() { return new Reserva("RES-000001","Una actividad con texto largo para revisar su detalle",LocalDate.of(2040,1,2),LocalTime.of(10,0),LocalTime.of(11,0),"otro",EstadoReserva.ACTIVA); }
    static JList<?> findList(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JList<?> list) return list;
            if (child instanceof Container nested) { JList<?> list = findList(nested); if (list != null) return list; }
        }
        return null;
    }
    static void layout(Container c) { c.doLayout(); for (Component child : c.getComponents()) if (child instanceof Container nested) layout(nested); }
}
