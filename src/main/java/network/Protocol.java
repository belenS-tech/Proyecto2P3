package network;

import java.io.*;
import model.Reserva;

/** Mensajes del protocolo v1; las respuestas se correlacionan por identificador. */
public final class Protocol {
    private Protocol() { }
    public record Request(long id, String service, String method, String[] types, Object[] args) implements Serializable { }
    public record Response(long id, Object value, String error) implements Serializable { }
    public record Alert(String author, Reserva reserva) implements Serializable { }
    public static void filter(ObjectInputStream input) {
        input.setObjectInputFilter(info -> {
            if (info.depth() > 40 || info.references() > 200000 || info.arrayLength() > 200000)
                return ObjectInputFilter.Status.REJECTED;
            Class<?> c = info.serialClass();
            if (c == null) return ObjectInputFilter.Status.UNDECIDED;
            while (c.isArray()) c = c.getComponentType();
            String n = c.getName();
            return c.isPrimitive() || n.startsWith("java.lang.") || n.startsWith("java.util.")
                    || n.startsWith("java.time.") || n.startsWith("model.") || n.startsWith("network.Protocol$")
                    || n.equals("service.AiReservationResponse")
                    ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
        });
    }
}
