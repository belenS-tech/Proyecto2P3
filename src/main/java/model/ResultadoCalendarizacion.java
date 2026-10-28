package model;

import java.util.List;
public class ResultadoCalendarizacion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Recurso> recursos;
    private final List<FilaCalendarizacion> filas;

    public ResultadoCalendarizacion(List<Recurso> recursos, List<FilaCalendarizacion> filas) {
        this.recursos = recursos;
        this.filas = filas;
    }

   public List<Recurso> getRecursos() {
        return recursos;
    }

    public List<FilaCalendarizacion> getFilas() {
        return filas;
    }
    public boolean estaVacio(){
        return recursos.isEmpty();
    }
}


