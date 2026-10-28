package repository;

import database.Database;
import model.*;
import java.sql.Date;
import java.time.*;
import java.util.*;

public class ReservaJdbcRepository extends JdbcSupport implements ReservaRepository {
    private static final String SELECT = "SELECT id,actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado FROM p3_reservas";

    public List<Reserva> listar() { return consultar(" ORDER BY fecha,hora_inicio,id"); }
    public Reserva buscarPorId(String id) { return first(consultar(" WHERE id=?",id)); }

    private List<Reserva> consultar(String where, Object... values) {
        return Database.use(c -> {
            List<Reserva> reservas = query(SELECT + where, r -> new Reserva(r.getString("id"),r.getString("actividad"),
                    r.getDate("fecha").toLocalDate(),LocalTime.parse(r.getString("hora_inicio")),
                    LocalTime.parse(r.getString("hora_fin")),r.getString("funcionario_id"),EstadoReserva.valueOf(r.getString("estado"))),values);
            for (Reserva reserva : reservas) {
                reserva.setDetalles(query("SELECT c.id,c.descripcion,r.id recurso_id,r.descripcion recurso_descripcion "
                        + "FROM p3_detalles d JOIN p3_recursos r ON r.id=d.recurso_id "
                        + "JOIN p3_categorias c ON c.id=r.categoria_id WHERE d.reserva_id=? ORDER BY r.id", r -> {
                    CategoriaRecurso cat = new CategoriaRecurso(r.getString("id"),r.getString("descripcion"));
                    return new DetalleReserva(cat,new Recurso(r.getString("recurso_id"),cat,r.getString("recurso_descripcion")));
                },reserva.getId()));
            }
            return reservas;
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
