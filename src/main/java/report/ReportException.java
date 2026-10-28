package report;

//excepcion para errores ocurridos durante
// la generacion de reportes pdf

public class ReportException extends Exception {

    public ReportException(String mensaje){
        super(mensaje);
    }

    public ReportException(String mensaje, Throwable causa){
        super(mensaje, causa);
    }
}
