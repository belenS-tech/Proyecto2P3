package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface EstadisticasRecursosService {
    Map<String, Integer> contarPorCategoria(LocalDate desde, LocalDate hasta);
}
