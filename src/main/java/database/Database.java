package database;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

/** Conexiones independientes y transacciones compartidas por hilo. */
public final class Database {
    private static final ThreadLocal<Connection> CURRENT = new ThreadLocal<>();
    private record Settings(String url, String user, String password) { }
    private static final java.util.concurrent.ConcurrentMap<Settings, ConnectionPool> POOLS = new java.util.concurrent.ConcurrentHashMap<>();
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> POOLS.values().forEach(ConnectionPool::closeIdle), "oracle-cerrar-pool"));
    }
    private Database() { }

    @FunctionalInterface public interface Work<T> { T run(Connection connection) throws SQLException; }

    public static Connection open() throws SQLException {
        Properties config = new Properties();
        Path file = DatabaseConfiguration.findFile();
        if (System.getProperty("db.config") != null && !Files.isRegularFile(file))
            throw new IllegalArgumentException("No existe el archivo indicado en db.config: " + file);
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) { config.load(in); }
            catch (java.io.IOException e) { throw new IllegalArgumentException("No se pudo leer database.properties", e); }
        }
        String url = setting(config, "db.url", "DB_URL", "jdbc:oracle:thin:@//localhost:1521/XE");
        String user = setting(config, "db.user", "DB_USER", "");
        String password = setting(config, "db.password", "DB_PASSWORD", "");
        if (user.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Configure db.user y db.password en " + file
                    + " (ver database.properties.example).");
        }
        Properties login = new Properties();
        login.setProperty("user", user);
        login.setProperty("password", password);
        login.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        login.setProperty("oracle.jdbc.ReadTimeout", "15000");
        Settings settings = new Settings(url,user,password);
        return POOLS.computeIfAbsent(settings, key -> new ConnectionPool(4, () -> DriverManager.getConnection(url, login))).borrow();
    }

    private static String setting(Properties p, String key, String env, String fallback) {
        return System.getProperty(key, System.getenv().getOrDefault(env, p.getProperty(key, fallback)));
    }

    public static <T> T use(Work<T> work) {
        Connection current = CURRENT.get();
        if (current != null) {
            try { return work.run(current); } catch (SQLException e) { throw failure(e); }
        }
        try (Connection c = open()) {
            CURRENT.set(c);
            try { return work.run(c); } finally { CURRENT.remove(); }
        }
        catch (SQLException e) { throw failure(e); }
    }

    public static <T> T transaction(Work<T> work) {
        if (CURRENT.get() != null) {
            try { return inTransaction(CURRENT.get(),work); }
            catch (SQLException e) { throw failure(e); }
        }
        try (Connection c = open()) {
            CURRENT.set(c);
            try { return inTransaction(c,work); } finally { CURRENT.remove(); }
        } catch (SQLException e) { throw failure(e); }
    }

    private static <T> T inTransaction(Connection c, Work<T> work) throws SQLException {
        if (!c.getAutoCommit()) return work.run(c);
        c.setAutoCommit(false);
        boolean completed = false;
        try {
            T result = work.run(c); c.commit(); completed = true; return result;
        } catch (SQLException | RuntimeException e) {
            try { c.rollback(); completed = true; } catch (SQLException rollback) { e.addSuppressed(rollback); }
            throw e;
        } finally { if (completed) c.setAutoCommit(true); }
    }

    private static IllegalArgumentException failure(SQLException e) {
        String message = switch (e.getErrorCode()) {
            case 1 -> "Ya existe un registro con ese identificador.";
            case 2291 -> "El registro relacionado no existe (usuario, categoría o recurso).";
            case 2292 -> "No se puede eliminar: el registro tiene datos relacionados.";
            case 2290, 1400, 12899 -> "Los datos no cumplen las restricciones de la base de datos.";
            case 942 -> "Faltan las tablas de Oracle. Ejecute database/01_schema.sql.";
            case 1017 -> "Oracle rechazó el usuario o la contraseña configurados.";
            default -> "No se pudo completar la operación en Oracle (código " + e.getErrorCode()
                    + "). Revise la conexión y que Oracle esté iniciado.";
        };
        return new IllegalArgumentException(message, e);
    }
}
