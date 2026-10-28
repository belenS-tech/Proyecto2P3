package api;

import model.*;
import java.time.*;
import java.util.*;
import exception.AiReservationException;
import service.AiReservationResponse;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface AiReservationService {
    AiReservationResponse interpretarSolicitud(String solicitud) throws AiReservationException;
}
