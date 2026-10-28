package util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;


public class DateUtils {

    /** Solapamiento con [hora, hora + 1h), incluida la fila 23:00 sin volver a 00:00. */
    public static boolean ocupaHora(LocalTime inicio, LocalTime fin, LocalTime hora) {
        int filaInicio = hora.toSecondOfDay();
        return inicio.toSecondOfDay() < filaInicio + 3600 && fin.toSecondOfDay() > filaInicio;
    }

    public static LocalDate inicioSemana(LocalDate fechaReferencia) {
        return fechaReferencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate finSemana(LocalDate fechaReferencia) {
        return inicioSemana(fechaReferencia).plusDays(6);
    }

    public static String etiquetaSemana(LocalDate fechaReferencia) {
        LocalDate inicio = inicioSemana(fechaReferencia);
        LocalDate fin = finSemana(fechaReferencia);
        return "Semana del " + inicio + " al " + fin;
    }
}
