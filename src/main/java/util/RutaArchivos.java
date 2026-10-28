package util;

import java.io.File;
import java.net.URISyntaxException;

public class RutaArchivos {

    private static final String BASE;

    static {
        try {
            File clases = new File(RutaArchivos.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            // clases apunta a target/classes, subimos hasta la raíz del proyecto
            BASE = clases.getParentFile().getParentFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("No se pudo determinar la ruta base del proyecto.", e);
        }
    }

    public static File get(String rutaRelativa) {
        return new File(BASE, rutaRelativa);
    }
}
