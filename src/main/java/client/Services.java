package client;

/** Composición del frontend. No conoce persistencia ni credenciales de Oracle. */
public final class Services {
    private static ServiceProxy connection;
    private Services() { }
    public static synchronized ServiceProxy connection() {
        if (connection == null) {
            try {
                connection = new ServiceProxy(System.getProperty("server.host", "localhost"),
                        Integer.getInteger("server.port", 5050));
            } catch (java.io.IOException e) {
                throw new IllegalArgumentException("No se pudo conectar al servidor. Inicie primero el backend y revise server.host y server.port.", e);
            }
        }
        return connection;
    }
    public static <T> T get(Class<T> type) { return connection().service(type); }
    public static synchronized void disconnect() {
        if (connection != null) connection.close();
        connection = null;
    }
}
