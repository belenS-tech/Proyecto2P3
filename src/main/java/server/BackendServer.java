package server;

import model.*;
import network.Protocol;
import network.Protocol.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Servidor sin interfaz. Cada conexión posee sesión, lector y cola de salida propios. */
public final class BackendServer implements AutoCloseable {
    private final ServerSocket listener;
    private final Map<Class<?>, Object> services;
    private final Set<Client> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final Object mutations = new Object();
    private volatile boolean closed;

    public BackendServer(String bind, int port, Map<Class<?>, Object> services) throws IOException {
        this.services = Map.copyOf(services);
        listener = new ServerSocket();
        listener.bind(new InetSocketAddress(bind, port));
    }
    public int port() { return listener.getLocalPort(); }
    public void serve() throws IOException {
        while (!closed) {
            try {
                Socket socket = listener.accept();
                Client client = new Client(socket); clients.add(client); workers.submit(client::run);
            } catch (SocketException e) { if (!closed) throw e; }
        }
    }
    private final class Client implements AutoCloseable {
        private final Socket socket;
        private final BlockingQueue<Object> outgoing = new ArrayBlockingQueue<>(256);
        private volatile Usuario user;
        private final Object sessionLock = new Object();
        private volatile boolean stopped;
        Client(Socket socket) { this.socket = socket; }
        void run() {
            try (socket) {
                socket.setSoTimeout(10000);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); output.flush();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream()); Protocol.filter(input);
                socket.setSoTimeout(0);
                workers.submit(() -> {
                    try {
                        while (!stopped) {
                            Object message = outgoing.take();
                            output.writeObject(message); output.flush(); output.reset();
                        }
                    } catch (IOException | InterruptedException e) { close(); }
                });
                while (!stopped) {
                    Object message = input.readObject();
                    if (!(message instanceof Request request)) throw new IOException("Petición inválida");
                    try {
                        Object result = dispatch(request);
                        send(new Response(request.id(), result, null));
                    } catch (Exception e) {
                        Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;
                        String error = cause instanceof IllegalArgumentException || cause instanceof IllegalStateException
                                || cause instanceof exception.AiReservationException ? cause.getMessage() : "No se pudo completar la operación.";
                        send(new Response(request.id(), null, error));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // Desconexión normal: elimina la sesión y despierta el escritor.
            } finally { close(); }
        }
        Object dispatch(Request r) throws Exception {
            if (r.service().equals("Session") && r.method().equals("logout")) {
                synchronized (sessionLock) { user = null; }
                service.SessionManager.getInstancia().cerrarSesion(); return null;
            }
            Class<?> contract = services.keySet().stream().filter(c -> c.getSimpleName().equals(r.service()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Servicio desconocido."));
            Method method = Arrays.stream(contract.getMethods()).filter(m -> m.getName().equals(r.method())
                    && Arrays.equals(Arrays.stream(m.getParameterTypes()).map(Class::getName).toArray(String[]::new), r.types()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Operación desconocida."));
            Object service = services.get(contract);
            if (contract == api.AuthService.class) {
                // El borrado no puede intercalarse entre autenticar y publicar la
                // sesión, ni entre leer un usuario y guardar su nueva contraseña.
                synchronized (mutations) {
                    if (r.method().equals("iniciarSesion")) {
                        synchronized (sessionLock) { user = null; }
                        Usuario logged = (Usuario) method.invoke(service, r.args());
                        synchronized (sessionLock) { user = new Usuario(logged.getId(), "", logged.getRol()); }
                        return user;
                    }
                    if (r.method().equals("cambiarClave") && r.args().length == 3) {
                        Usuario current = requireLogin();
                        ((api.AuthService) service).cambiarClave(current.getId(), (String) r.args()[0], (String) r.args()[1], (String) r.args()[2]);
                        return null;
                    }
                    return method.invoke(service, r.args());
                }
            }
            Usuario currentUser = requireLogin();
            // Las consultas administrativas tienen el mismo límite de rol que
            // sus escrituras. Listar categorías sí es necesario para reservar
            // y consultar el calendario desde cualquiera de los dos roles.
            boolean administrative = contract == api.FuncionarioService.class
                    || contract == api.RecursoService.class
                    || (contract == api.CategoriaService.class && !r.method().equals("listarTodos"));
            if (administrative && currentUser.getRol() != Rol.ADMINISTRADOR)
                throw new IllegalArgumentException("Esta operación requiere un administrador.");
            boolean mutation = Set.of("incluir", "modificar", "eliminar", "registrar", "actualizar").contains(r.method());
            if (mutation && contract != api.ReservaService.class && currentUser.getRol() != Rol.ADMINISTRADOR)
                throw new IllegalArgumentException("Esta operación requiere un administrador.");
            if (mutation && contract == api.ReservaService.class) {
                if (currentUser.getRol() != Rol.FUNCIONARIO) throw new IllegalArgumentException("Esta operación requiere un funcionario.");
                Reserva reservation = (Reserva) r.args()[0];
                if (reservation == null || !currentUser.getId().equals(reservation.getFuncionarioId()))
                    throw new IllegalArgumentException("Sólo puede modificar sus propias reservas.");
            }
            Object result;
            if (mutation) {
                synchronized (mutations) {
                    // La cuenta pudo eliminarse mientras esta solicitud esperaba.
                    requireLogin();
                    if (contract == api.ReservaService.class && r.method().equals("registrar")) {
                        Reserva reservation = (Reserva) r.args()[0];
                        if (reservation.getId() == null || reservation.getId().isBlank()) {
                            long next = 1;
                            for (Reserva existing : ((api.ReservaService) service).listarTodos()) {
                                try { next = Math.max(next, Long.parseLong(existing.getId().replaceFirst("^RES-", "")) + 1); }
                                catch (NumberFormatException ignored) { }
                            }
                            reservation.setId("RES-" + String.format("%06d", next));
                        }
                    }
                    if (contract == api.ReservaService.class && !r.method().equals("registrar")) {
                        Reserva existing = ((api.ReservaService) service).buscarPorId(((Reserva) r.args()[0]).getId());
                        if (existing == null || !currentUser.getId().equals(existing.getFuncionarioId()))
                            throw new IllegalArgumentException("Sólo puede modificar sus propias reservas.");
                    }
                    result = method.invoke(service, r.args());
                    if (contract == api.FuncionarioService.class && r.method().equals("eliminar")) {
                        String deletedId = ((String) r.args()[0]).trim();
                        for (Client client : clients) client.revoke(deletedId);
                    }
                    if (contract == api.ReservaService.class && r.method().equals("registrar")) {
                        Alert alert = new Alert(currentUser.getId(), (Reserva) r.args()[0]);
                        for (Client other : clients) {
                            if (other != this) other.sendAlert(alert);
                        }
                    }
                }
            } else result = method.invoke(service, r.args());
            return result;
        }
        Usuario requireLogin() {
            Usuario current = user;
            if (current == null) throw new IllegalArgumentException("Debe iniciar sesión.");
            return current;
        }
        void revoke(String id) {
            synchronized (sessionLock) {
                if (user != null && user.getId().equals(id)) close();
            }
        }
        void sendAlert(Alert alert) {
            synchronized (sessionLock) {
                if (user != null && !user.getId().equals(alert.author())) send(alert);
            }
        }
        void send(Object message) { if (!stopped && !outgoing.offer(message)) close(); }
        @Override public void close() {
            stopped = true; user = null; clients.remove(this);
            try { socket.close(); } catch (IOException ignored) { }
            // El escritor podría estar esperando en take().
            outgoing.offer(new Response(-1, null, "Conexión cerrada"));
        }
    }
    @Override public void close() throws IOException {
        closed = true; listener.close(); clients.forEach(Client::close); workers.shutdownNow();
    }
}
