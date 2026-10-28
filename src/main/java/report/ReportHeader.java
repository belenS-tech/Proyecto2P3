package report;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Encapsula la información comun que debe aparecer en el encabezado
//de cualquier reporte del sistema

public class ReportHeader {
    private static final String NOMBRE_SISTEMA = "Sistema de Reserva de Recursos";
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); //formato para la fecha y hora en tiempo real

    private final String titulo;
    private final String usuarioGenera;
    private final String filtrosAplicados;
    private final LocalDateTime fechaGeneracion;

    //debe tener un titulo y usuario
    public ReportHeader(String titulo, String usuarioGenera, String filtrosAplicados) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del reporte no puede estar vacío.");
        }
        if (usuarioGenera == null || usuarioGenera.isBlank()) {
            throw new IllegalArgumentException("El usuario que genera el reporte es obligatorio.");
        }
        this.titulo = titulo;
        this.usuarioGenera = usuarioGenera;
        this.filtrosAplicados = filtrosAplicados;
        this.fechaGeneracion = LocalDateTime.now();
    }

    //getters
    public String getNombreSistema(){
        return NOMBRE_SISTEMA;
    }

    public String getTitulo(){
        return titulo;
    }

    public String getUsuarioGenera(){
        return usuarioGenera;
    }

    public String getFiltrosAplicados(){

        return filtrosAplicados;
    }

    //filtros
    public boolean tieneFiltros(){
        return filtrosAplicados != null && !filtrosAplicados.isBlank();
    }

    public LocalDateTime getFechaGeneracion(){
        return fechaGeneracion;
    }

    public String getFechaGeneracionFormateada(){
        return fechaGeneracion.format(FORMATO_FECHA_HORA);
    }
}
