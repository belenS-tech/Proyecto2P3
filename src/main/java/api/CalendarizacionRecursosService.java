package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface CalendarizacionRecursosService {
    ResultadoCalendarizacion construirMatriz(LocalDate fecha, String categoriaId);
}
