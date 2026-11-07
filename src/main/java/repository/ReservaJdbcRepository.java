package repository;

import database.Database;
import model.*;
import java.sql.Date;
import java.time.*;
import java.util.*;

public class ReservaJdbcRepository extends JdbcSupport implements ReservaRepository {
    private static final String SELECT = """
            SELECT v.id, v.actividad, v.fecha, v.hora_inicio, v.hora_fin, v.funcionario_id, v.estado,
                   c.id categoria_id, c.descripcion categoria_descripcion,
                   r.id recurso_id, r.descripcion recurso_descripcion
            FROM p3_reservas v
            LEFT JOIN p3_detalles d ON d.reserva_id = v.id
            LEFT JOIN p3_recursos r ON r.id = d.recurso_id
            LEFT JOIN p3_categorias c ON c.id = r.categoria_id
            """;
    private static final String ORDEN = " ORDER BY v.fecha, v.hora_inicio, v.id, r.id";

    public List<Reserva> listar() { return consultar(""); }
    public Reserva buscarPorId(String id) { return first(consultar(" WHERE v.id = ?", id)); }

    private List<Reserva> consultar(String where, Object... values) {
        return Database.use(c -> {
            try (var consulta = c.prepareStatement(SELECT + where + ORDEN)) {
                for (int i = 0; i < values.length; i++) consulta.setObject(i + 1, values[i]);
                try (var filas = consulta.executeQuery()) {
                    List<Reserva> reservas = new ArrayList<>();
                    Reserva reserva = null;
                    while (filas.next()) {
                        String id = filas.getString("id");
                        if (reserva == null || !reserva.getId().equals(id)) {
                            reserva = new Reserva(id, filas.getString("actividad"), filas.getDate("fecha").toLocalDate(),
                                    LocalTime.parse(filas.getString("hora_inicio")), LocalTime.parse(filas.getString("hora_fin")),
                                    filas.getString("funcionario_id"), EstadoReserva.valueOf(filas.getString("estado")));
                            reservas.add(reserva);
                        }
                        String recursoId = filas.getString("recurso_id");
                        if (recursoId != null) {
                            var categoria = new CategoriaRecurso(filas.getString("categoria_id"), filas.getString("categoria_descripcion"));
                            var recurso = new Recurso(recursoId, categoria, filas.getString("recurso_descripcion"));
                            reserva.getDetalles().add(new DetalleReserva(categoria, recurso));
                        }
                    }
                    return reservas;
                }
            }
        });
    }

    public void guardar(Reserva r) { escribir(r,false); }
    public void actualizar(Reserva r) { escribir(r,true); }

    private void escribir(Reserva r, boolean actualizar) {
        Database.transaction(c -> {
            validarDisponibilidad(r);
            Object[] values = {r.getActividad(),Date.valueOf(r.getFecha()),hora(r.getHoraInicio()),hora(r.getHoraFin()),r.getFuncionarioId(),r.getEstado().name(),r.getId()};
            if (actualizar) {
                if (execute("UPDATE p3_reservas SET actividad=?,fecha=?,hora_inicio=?,hora_fin=?,funcionario_id=?,estado=? WHERE id=?",values) == 0)
                    throw new IllegalArgumentException("No existe una reserva con ese ID.");
                execute("DELETE FROM p3_detalles WHERE reserva_id=?",r.getId());
            } else {
                execute("INSERT INTO p3_reservas (actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado,id) VALUES (?,?,?,?,?,?,?)",values);
            }
            for (DetalleReserva d : r.getDetalles()) {
                execute("INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES (?,?)",r.getId(),d.getRecurso().getId());
            }
            return null;
        });
    }

    private static String hora(LocalTime time) { return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")); }

    /** Bloqueo en orden estable: dos conexiones no pueden reservar simultáneamente el mismo recurso. */
    private void validarDisponibilidad(Reserva r) {
        for (String id : r.getDetalles().stream().map(d -> d.getRecurso().getId()).distinct().sorted().toList()) {
            if (query("SELECT id FROM p3_recursos WHERE id=? FOR UPDATE",s -> s.getString(1),id).isEmpty())
                throw new IllegalArgumentException("El recurso ya no existe.");
            if (r.getEstado() == EstadoReserva.ACTIVA && !query("SELECT v.id FROM p3_reservas v JOIN p3_detalles d ON d.reserva_id=v.id "
                    + "WHERE d.recurso_id=? AND v.fecha=? AND v.estado='ACTIVA' AND v.id<>? AND v.hora_inicio<? AND v.hora_fin>?",
                    s -> s.getString(1),id,Date.valueOf(r.getFecha()),r.getId(),hora(r.getHoraFin()),hora(r.getHoraInicio())).isEmpty())
                throw new IllegalArgumentException("El recurso no está disponible en ese horario.");
        }
    }

    public void eliminar(Reserva r) {
        if (execute("DELETE FROM p3_reservas WHERE id=?",r.getId()) == 0)
            throw new IllegalArgumentException("No existe una reserva con ese ID.");
    }
}
