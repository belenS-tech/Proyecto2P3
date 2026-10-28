package database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigurationTest {
    @TempDir Path temporary;

    @Test void intellijDesdeLaCarpetaExteriorEncuentraLaConfiguracionDelModulo() throws Exception {
        Path project = Files.createDirectories(temporary.resolve("proyecto/modulo"));
        Path classes = Files.createDirectories(project.resolve("target/classes"));
        Path config = Files.createFile(project.resolve("database.properties"));
        assertEquals(config, DatabaseConfiguration.findFile(temporary, classes, null));
    }

    @Test void respetaPrimeroElArchivoDelDirectorioDeTrabajo() throws Exception {
        Path classes = Files.createDirectories(temporary.resolve("modulo/target/classes"));
        Files.createFile(temporary.resolve("modulo/database.properties"));
        Path local = Files.createFile(temporary.resolve("database.properties"));
        assertEquals(local, DatabaseConfiguration.findFile(temporary, classes, null));
    }

    @Test void encuentraElArchivoJuntoAlJarAunqueSeEjecuteDesdeOtraCarpeta() throws Exception {
        Path directory = Files.createDirectory(temporary.resolve("distribucion"));
        Path jar = Files.createFile(directory.resolve("backend.jar"));
        Path config = Files.createFile(directory.resolve("database.properties"));
        assertEquals(config, DatabaseConfiguration.findFile(temporary, jar, null));
    }

    @Test void laRutaExplicitaNoSeSustituyePorOtraConfiguracion() throws Exception {
        Files.createFile(temporary.resolve("database.properties"));
        assertEquals(temporary.resolve("otra.properties"),
                DatabaseConfiguration.findFile(temporary, temporary, "otra.properties"));
    }

    @Test void sinArchivoIndicaLaRutaEsperadaEnElDirectorioDeTrabajo() {
        assertEquals(temporary.resolve("database.properties"),
                DatabaseConfiguration.findFile(temporary, null, null));
    }
}
