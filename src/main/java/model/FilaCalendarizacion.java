package model;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa una fila de la matriz de calendarización: una hora del
 * día y el texto que corresponde a cada recurso en esa hora (vacío
 * si el recurso está libre).
 *  se usa para mostrar la matriz en la tabla y luego en el reporte PDF.
 */
public class FilaCalendarizacion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final String horaTexto;
    private final Map<String, String> valoresPorRecursoId;

    public FilaCalendarizacion(String horaTexto) {
        this.horaTexto = horaTexto;
        this.valoresPorRecursoId = new LinkedHashMap<>();
    }

    public String getHoraTexto() {
        return horaTexto;
    }

    public void setValor(String recursoId, String texto) {
        valoresPorRecursoId.put(recursoId, texto);
    }

    public String getValor(String recursoId) {
        return valoresPorRecursoId.getOrDefault(recursoId, "");
    }
}