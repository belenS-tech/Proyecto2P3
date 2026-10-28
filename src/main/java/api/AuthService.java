package api;

import model.*;
import java.time.*;
import java.util.*;

/** Contrato compartido por el servicio y su Proxy remoto. */
public interface AuthService {
    Usuario iniciarSesion(String id, String clave);
    void verificarExiste(String id);
    void cambiarClave(String id, String claveActual, String claveNueva, String confirmacion);
    void cambiarClave(String claveActual, String claveNueva, String confirmacion);
}
