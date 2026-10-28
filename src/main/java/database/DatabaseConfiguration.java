package database;

import java.nio.file.Files;
import java.nio.file.Path;

/** Localiza la configuración externa sin depender del directorio de IntelliJ. */
final class DatabaseConfiguration {
    private DatabaseConfiguration() { }

    static Path findFile(Path workingDirectory, Path codeLocation, String explicitFile) {
        if (explicitFile != null) {
            Path explicit = Path.of(explicitFile);
            return (explicit.isAbsolute() ? explicit : workingDirectory.resolve(explicit)).normalize();
        }
        Path local = workingDirectory.resolve("database.properties").normalize();
        if (Files.isRegularFile(local)) return local;
        if (codeLocation != null) {
            Path directory = codeLocation;
            if (Files.isRegularFile(directory)) directory = directory.getParent();
            // target/classes -> target -> carpeta del pom.xml; o junto al JAR.
            for (int i = 0; i < 3 && directory != null; i++, directory = directory.getParent()) {
                Path candidate = directory.resolve("database.properties");
                if (Files.isRegularFile(candidate)) return candidate.normalize();
            }
        }
        return local;
    }

    static Path findFile() {
        Path location = null;
        try {
            var source = Database.class.getProtectionDomain().getCodeSource();
            if (source != null) location = Path.of(source.getLocation().toURI());
        } catch (java.net.URISyntaxException | IllegalArgumentException | SecurityException ignored) { }
        return findFile(Path.of("").toAbsolutePath(), location, System.getProperty("db.config"));
    }
}
