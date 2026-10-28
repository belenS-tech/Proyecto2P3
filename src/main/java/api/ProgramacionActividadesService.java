package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface ProgramacionActividadesService {
    ResultadoProgramacionActividades construirMatrizSemana(LocalDate fechaReferencia);
}
