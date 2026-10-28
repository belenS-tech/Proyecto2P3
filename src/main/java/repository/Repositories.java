package repository;

/** Punto único de selección de persistencia. La aplicación utiliza Oracle. */
public final class Repositories {
    private Repositories() { }
    public static UsuarioRepository usuarios() { return new UsuarioJdbcRepository(); }
    public static FuncionarioRepository funcionarios() { return new FuncionarioJdbcRepository(); }
    public static CategoriaRepository categorias() { return new CategoriaJdbcRepository(); }
    public static RecursoRepository recursos() { return new RecursoJdbcRepository(); }
    public static ReservaRepository reservas() { return new ReservaJdbcRepository(); }
    public static void atomic(Object repository, Runnable action) {
        if (repository instanceof JdbcSupport) {
            database.Database.transaction(c -> { action.run(); return null; });
        } else action.run();
    }
}
