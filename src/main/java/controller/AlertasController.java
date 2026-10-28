package controller;

import client.ServiceProxy;
import network.Protocol.Alert;
import view.AlertasPanel;
import javax.swing.*;
import java.util.function.Consumer;

/** Adapta el receptor asíncrono al hilo de Swing y administra su suscripción. */
public final class AlertasController implements AutoCloseable {
    private final ServiceProxy connection;
    private final Consumer<Alert> listener;
    private volatile boolean closed;
    public AlertasController(AlertasPanel view, ServiceProxy connection) {
        this.connection = connection;
        listener = alert -> SwingUtilities.invokeLater(() -> { if (!closed) view.agregar(alert); });
        connection.addAlertListener(listener);
        view.ver().addActionListener(e -> { Alert a = view.seleccionada(); if (a != null) view.mostrarDetalle(a.reserva()); });
        view.descartar().addActionListener(e -> {
            if (view.seleccionada() != null && view.confirmarDescarte()) view.quitarSeleccionada();
        });
    }
    @Override public void close() { closed = true; connection.removeAlertListener(listener); }
}
