package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface EstadisticasActividadesService {
    LinkedHashMap<String, Integer> calcular(LocalDate desde, LocalDate hasta);
}
