package report;

import java.util.ArrayList;
import java.util.List;

//esta clase representa el cuerpo de un reporte en forma de tabla o matriz
public class ReportTable {
    private final List<String> encabezados;
    private final List<List<String>> filas;

    public ReportTable(List<String> encabezados) {
        if (encabezados == null || encabezados.isEmpty()) {
            throw new IllegalArgumentException("La tabla debe tener al menos un encabezado.. ");

        }
        this.encabezados = new ArrayList<>(encabezados);
        this.filas = new ArrayList<>();
    }

    //agregar las filas respectivas de la matriz
    public void agregarFila(List<String> valores) {
        if (valores == null || valores.size() != encabezados.size()) {
            throw new IllegalArgumentException(
                    "La fila debe tener " + encabezados.size() + " columnas, recibidas: "
                            + (valores == null ? 0 : valores.size()));
        }
        List<String> filaLimpia = new ArrayList<>();
        for (String valor : valores) {
            filaLimpia.add(valor == null ? "" : valor);
        }
        filas.add(filaLimpia);
    }

    public void agregarFila(String... valores) {
        agregarFila(List.of(valores));
    }

    //getters necesarios
    public List<String> getEncabezados() {
        return encabezados;
    }

    public List<List<String>> getFilas() {
        return filas;
    }

    public int cantidadColumnas() {
        return encabezados.size();
    }

    public boolean estaVacia() {
        return filas.isEmpty();
    }
}



