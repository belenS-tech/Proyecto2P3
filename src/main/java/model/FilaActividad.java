package model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilaActividad implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final String horaTexto;
    private final Map<LocalDate, String> valorDia;

    public FilaActividad(String horaTexto) {
        this.horaTexto = horaTexto;
        this.valorDia = new LinkedHashMap<>();
    }

    public String getHoraTexto() {
        return horaTexto;
    }

    public void setValor(LocalDate dia, String texto){
        valorDia.put(dia, texto);
    }

    public String getValor(LocalDate dia){
        return valorDia.getOrDefault(dia, "");
    }

}
