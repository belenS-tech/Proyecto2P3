package exception;

public class AiReservationException extends Exception {

    public AiReservationException(String mensaje) {
        super(mensaje);
    }

    public AiReservationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}