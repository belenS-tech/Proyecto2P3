package model;

import java.time.LocalDate;
import java.util.List;

//contruir la matriz semanal

public class ResultadoProgramacionActividades implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final List<LocalDate> dias;
    private final List<FilaActividad> filas;

    public ResultadoProgramacionActividades(List<LocalDate> dias, List<FilaActividad> filas) {
        this.dias = dias;
        this.filas = filas;
    }

    public List<LocalDate> getDias() {
        return dias;
    }

    public List<FilaActividad> getFilas() {
        return filas;
    }
}
