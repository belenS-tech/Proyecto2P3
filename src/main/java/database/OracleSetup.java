package database;

import java.nio.file.*;
import java.sql.*;

/** Utilidad de consola: check verifica; install crea únicamente si no hay tablas P3_. */
public final class OracleSetup {
    private static final String[] TABLES = {"P3_USUARIOS","P3_FUNCIONARIOS","P3_CATEGORIAS","P3_RECURSOS","P3_RESERVAS","P3_DETALLES"};
    public static void main(String[] args) throws Exception {
        String action = args.length == 0 ? "check" : args[0];
        if (!action.equals("check") && !action.equals("install")) throw new IllegalArgumentException("Use check o install.");
        try (Connection c = Database.open()) {
            System.out.println("Conexión correcta: " + c.getMetaData().getDatabaseProductVersion());
            if (action.equals("install")) {
                for (String table : TABLES) {
                    try (PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM user_tables WHERE table_name=?")) {
                        s.setString(1,table);
                        try (ResultSet rs = s.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) != 0) throw new IllegalStateException("Ya existe " + table + ". Se cancela la instalación para conservar los datos.");
                        }
                    }
                }
                runScript(c, "database/01_schema.sql");
                c.setAutoCommit(false);
                try {
                    runScript(c, "database/02_datos_xml.sql");
                    c.commit();
                } catch (Exception e) { c.rollback(); throw e; }
                System.out.println("Tablas creadas e importación completada.");
            }
            for (String table : TABLES) {
                try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    rs.next();
                    System.out.println(table + ": " + rs.getInt(1));
                }
            }
        }
    }
    private static void runScript(Connection c, String file) throws Exception {
        // Estos scripts contienen DDL/DML simple, sin bloques PL/SQL ni ';' dentro de valores.
        StringBuilder sql = new StringBuilder();
        for (String line : Files.readAllLines(Path.of(file), java.nio.charset.StandardCharsets.UTF_8)) {
            String trimmed = line.strip();
            if (trimmed.startsWith("--") || trimmed.startsWith("WHENEVER ") || trimmed.startsWith("SET ") || trimmed.equals("COMMIT;")) continue;
            sql.append(line).append('\n');
        }
        // Separar respetando literales de texto (incluidas comillas escapadas).
        StringBuilder statement = new StringBuilder();
        boolean quoted = false;
        for (int i=0; i<sql.length(); i++) {
            char ch=sql.charAt(i);
            if (ch=='\'') {
                if (quoted && i+1<sql.length() && sql.charAt(i+1)=='\'') {
                    statement.append(ch).append(sql.charAt(++i));
                    continue;
                }
                quoted=!quoted;
            }
            if (ch==';' && !quoted) {
                if (!statement.toString().isBlank()) try (Statement s=c.createStatement()) { s.execute(statement.toString()); }
                statement.setLength(0);
            } else statement.append(ch);
        }
        if (!statement.toString().isBlank()) throw new IllegalArgumentException("Sentencia sin terminar en " + file);
    }
}
