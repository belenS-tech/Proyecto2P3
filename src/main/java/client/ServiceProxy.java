package client;

import network.Protocol.*;
import network.Protocol;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Proxy de servicios: un socket, un hilo receptor y solicitudes correlacionadas. */
public final class ServiceProxy implements AutoCloseable {
    private final Socket socket = new Socket();
    private final ObjectOutputStream output;
    private final ObjectInputStream input;
    private final AtomicLong sequence = new AtomicLong();
    private final Map<Long, CompletableFuture<Response>> pending = new ConcurrentHashMap<>();
    private final List<Consumer<Alert>> listeners = new CopyOnWriteArrayList<>();
    private final List<Alert> bufferedAlerts = new ArrayList<>();
    private volatile boolean closed;

    public ServiceProxy(String host, int port) throws IOException {
        try {
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(5000);
            output = new ObjectOutputStream(socket.getOutputStream()); output.flush();
            input = new ObjectInputStream(socket.getInputStream()); Protocol.filter(input);
            socket.setSoTimeout(0);
        } catch (IOException e) { socket.close(); throw e; }
        Thread reader = new Thread(this::receive, "reservas-receptor");
        reader.setDaemon(true); reader.start();
    }
    public <T> T service(Class<T> type) {
        if (!type.isInterface() || !type.getPackageName().equals("api")) throw new IllegalArgumentException("Contrato inválido");
        return type.cast(java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) return switch (method.getName()) {
                case "toString" -> "Proxy remoto: " + type.getSimpleName();
                case "hashCode" -> System.identityHashCode(proxy);
                default -> proxy == args[0];
            };
            Response response = call(type.getSimpleName(), method.getName(),
                    Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new), args);
            if (response.error() != null) {
                if (Arrays.asList(method.getExceptionTypes()).contains(exception.AiReservationException.class))
                    throw new exception.AiReservationException(response.error());
                throw new IllegalArgumentException(response.error());
            }
            return response.value();
        }));
    }
    private Response call(String service, String method, String[] types, Object[] args) {
        long id = sequence.incrementAndGet();
        CompletableFuture<Response> future = new CompletableFuture<>();
        pending.put(id, future);
        try {
            synchronized (output) {
                if (closed) throw new IOException("Conexión cerrada");
                output.writeObject(new Request(id, service, method, types, args == null ? new Object[0] : args));
                output.flush(); output.reset();
            }
            return future.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); throw new IllegalArgumentException("Solicitud interrumpida.", e);
        } catch (Exception e) {
            close();
            throw new IllegalArgumentException("Se perdió la conexión con el servidor. Reinicie el cliente y revise el resultado antes de repetir la operación.", e);
        } finally { pending.remove(id); }
    }
    private void receive() {
        try {
            while (!closed) {
                Object message = input.readObject();
                if (message instanceof Response r) {
                    CompletableFuture<Response> future = pending.get(r.id()); if (future != null) future.complete(r);
                } else if (message instanceof Alert alert) {
                    synchronized (bufferedAlerts) {
                        if (listeners.isEmpty()) bufferedAlerts.add(alert);
                        for (Consumer<Alert> listener : listeners) {
                            try { listener.accept(alert); } catch (RuntimeException ignored) { }
                        }
                    }
                } else throw new IOException("Mensaje desconocido");
            }
        } catch (IOException | ClassNotFoundException e) { close(); }
    }
    public void addAlertListener(Consumer<Alert> listener) {
        synchronized (bufferedAlerts) {
            listeners.add(listener);
            bufferedAlerts.forEach(listener);
            bufferedAlerts.clear();
        }
    }
    public void removeAlertListener(Consumer<Alert> listener) { listeners.remove(listener); }
    public void logout() {
        call("Session", "logout", new String[0], new Object[0]);
        synchronized (bufferedAlerts) { bufferedAlerts.clear(); }
    }
    @Override public void close() {
        closed = true;
        try { socket.close(); } catch (IOException ignored) { }
        pending.values().forEach(f -> f.completeExceptionally(new IOException("Servidor desconectado")));
    }
}
