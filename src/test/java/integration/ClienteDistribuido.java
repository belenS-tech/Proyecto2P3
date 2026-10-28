package integration;

import client.ServiceProxy;
import model.*;
import network.Protocol.Alert;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Cliente de prueba lanzado en otra JVM con el JAR frontend y sin JDBC. */
public final class ClienteDistribuido {
    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    public static void main(String[] args) throws Exception {
        for (String clase : List.of("database.Database", "repository.Repositories", "server.BackendServer")) {
            try { Class.forName(clase); throw new AssertionError("El cliente contiene " + clase); }
            catch (ClassNotFoundException esperado) { }
        }
        Properties resultado = new Properties();
        String modo = args[0], usuario = args[3];
        Path salida = Path.of(args[5]);
        try (var conexion = new ServiceProxy(args[1],Integer.parseInt(args[2]))) {
            conexion.service(api.AuthService.class).iniciarSesion(usuario,args[4]);
            var reservas = conexion.service(api.ReservaService.class);
            if (modo.equals("verificar")) {
                Reserva guardada = reservas.buscarPorId(args[6]);
                verificar(guardada != null,"La reserva no sobrevivió al reinicio");
                verificar(guardada.getFuncionarioId().equals(usuario),"Responsable incorrecto");
                resultado.setProperty("estado","PERSISTE");
                resultado.setProperty("reserva",guardada.getId());
            } else {
                Path lista = Path.of(args[6]), inicio = Path.of(args[7]);
                String categoriaId = args[8], actividad = args[9];
                CategoriaRecurso categoria = conexion.service(api.CategoriaService.class).listarTodos().stream()
                        .filter(c -> c.getId().equals(categoriaId)).findFirst().orElseThrow();
                BlockingQueue<Alert> alertas = new LinkedBlockingQueue<>(); conexion.addAlertListener(alertas::add);
                // Ambos procesos ya iniciaron sesión y se suscribieron antes de abrir la barrera.
                Files.writeString(lista,"LISTO");
                long limite = System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
                while (!Files.exists(inicio)) {
                    if (System.nanoTime()>limite) throw new AssertionError("La barrera no se abrió");
                    Thread.sleep(20);
                }
                Reserva solicitud = new Reserva("",actividad,LocalDate.of(2040,1,2),
                        LocalTime.of(10,45),LocalTime.of(11,17),usuario,EstadoReserva.ACTIVA);
                solicitud.setDetalles(List.of(new DetalleReserva(categoria,null)));
                try {
                    reservas.registrar(solicitud);
                    Reserva guardada = reservas.listarTodos().stream().filter(r -> actividad.equals(r.getActividad())).findFirst().orElseThrow();
                    verificar(guardada.getHoraInicio().equals(LocalTime.of(10,45)),"Inicio alterado");
                    verificar(guardada.getHoraFin().equals(LocalTime.of(11,17)),"Fin alterado");
                    resultado.setProperty("estado","OK"); resultado.setProperty("reserva",guardada.getId());
                    resultado.setProperty("recurso",guardada.getDetalles().getFirst().getRecurso().getId());
                } catch (IllegalArgumentException error) {
                    verificar(error.getMessage().contains("disponibilidad"),"Rechazo inesperado: "+error.getMessage());
                    resultado.setProperty("estado","SIN_DISPONIBILIDAD");
                }
                // Verificar que las notificaciones del otro proceso llegan sin consultar la lista.
                Alert aviso = alertas.poll(2,TimeUnit.SECONDS);
                int cantidad = 0;
                if (aviso != null) {
                    verificar(!usuario.equals(aviso.author()),"Se notificó al autor"); cantidad++;
                }
                for (Alert otra : alertas) { verificar(!usuario.equals(otra.author()),"Se notificó al autor"); cantidad++; }
                resultado.setProperty("alertas",String.valueOf(cantidad));
            }
            try (var archivo = Files.newOutputStream(salida)) { resultado.store(archivo,"Cliente de prueba aislado"); }
        }
    }
}
