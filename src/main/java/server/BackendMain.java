package server;

import repository.Repositories;
import java.util.Map;

/** Único proceso de la aplicación que configura persistencia y servicios reales. */
public final class BackendMain {
    public static Map<Class<?>, Object> services() {
        var usuarios = Repositories.usuarios(); var funcionarios = Repositories.funcionarios();
        var categorias = Repositories.categorias(); var recursos = Repositories.recursos(); var reservas = Repositories.reservas();
        var disponibilidad = new service.DisponibilidadService(recursos, reservas);
        return Map.ofEntries(
                Map.entry(api.AuthService.class, new service.AuthService(usuarios)),
                Map.entry(api.FuncionarioService.class, new service.FuncionarioService(funcionarios, usuarios, reservas)),
                Map.entry(api.CategoriaService.class, new service.CategoriaService(categorias, recursos, reservas)),
                Map.entry(api.RecursoService.class, new service.RecursoService(recursos)),
                Map.entry(api.ReservaService.class, new service.ReservaService(disponibilidad, reservas)),
                Map.entry(api.DisponibilidadService.class, disponibilidad),
                Map.entry(api.CalendarizacionRecursosService.class, new service.CalendarizacionRecursosService(recursos, reservas, funcionarios)),
                Map.entry(api.ProgramacionActividadesService.class, new service.ProgramacionActividadesService(reservas, funcionarios)),
                Map.entry(api.EstadisticasActividadesService.class, new service.EstadisticasActividadesService(reservas)),
                Map.entry(api.EstadisticasRecursosService.class, new service.EstadisticasRecursosService(reservas)),
                Map.entry(api.AiReservationService.class, new service.AiReservationService()));
    }
    public static void main(String[] args) throws Exception {
        // Fallar al inicio si Oracle no está configurado, antes de aceptar clientes.
        Repositories.usuarios().listarTodos();
        try (var server = new BackendServer(System.getProperty("server.bind", "0.0.0.0"),
                Integer.getInteger("server.port", 5050), services())) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { server.close(); } catch (Exception ignored) { } }));
            System.out.println("Backend de reservas escuchando en el puerto " + server.port());
            server.serve();
        }
    }
}
