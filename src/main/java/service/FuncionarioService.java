package service;

import model.Funcionario;
import model.Rol;
import model.Usuario;
import repository.FuncionarioRepository;
import repository.UsuarioRepository;

import java.util.List;
import java.util.stream.Collectors;

public class FuncionarioService implements api.FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final repository.ReservaRepository reservaRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              UsuarioRepository usuarioRepository) {
        this(funcionarioRepository, usuarioRepository, repository.Repositories.reservas());
    }

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              UsuarioRepository usuarioRepository,
                              repository.ReservaRepository reservaRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.listarTodos();
    }

    /** Busca por ID exacto o por coincidencia parcial en el nombre. */
    public List<Funcionario> buscar(String id, String nombre) {
        return funcionarioRepository.listarTodos().stream()
                .filter(f -> id == null || id.isBlank()
                        || f.getId().equalsIgnoreCase(id.trim()))
                .filter(f -> nombre == null || nombre.isBlank()
                        || f.getNombre().toLowerCase().contains(nombre.trim().toLowerCase()))
                .collect(Collectors.toList());
    }

    public Funcionario buscarPorId(String id) {
        return funcionarioRepository.buscarPorId(id);
    }

    /**
     * Registra un funcionario nuevo y crea automáticamente su usuario,
     * con la clave inicial igual al ID y rol FUNCIONARIO.
     */
    public void incluir(String id, String nombre, String telefono) {
        repository.Repositories.atomic(funcionarioRepository, () -> incluirInterno(id, nombre, telefono));
    }

    private void incluirInterno(String id, String nombre, String telefono) {
        validarDatos(id, nombre, telefono);

        if (funcionarioRepository.buscarPorId(id.trim()) != null) {
            throw new IllegalArgumentException("Ya existe un funcionario con ese ID.");
        }
        if (usuarioRepository.buscarPorId(id.trim()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID.");
        }

        Funcionario funcionario = new Funcionario(id.trim(), nombre.trim(), telefono.trim());
        funcionarioRepository.guardar(funcionario);

        Usuario usuario = new Usuario(id.trim(), id.trim(), Rol.FUNCIONARIO);
        usuarioRepository.guardar(usuario);
    }

    /** Modifica nombre y teléfono. El ID no se puede cambiar. */
    public void modificar(String id, String nombre, String telefono) {
        validarDatos(id, nombre, telefono);

        Funcionario funcionario = funcionarioRepository.buscarPorId(id.trim());
        if (funcionario == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }

        funcionario.setNombre(nombre.trim());
        funcionario.setTelefono(telefono.trim());
        funcionarioRepository.guardar(funcionario);
    }

    /** Elimina el funcionario, su usuario y todas sus reservas. */
    public void eliminar(String id) {
        repository.Repositories.atomic(funcionarioRepository, () -> eliminarInterno(id));
    }

    private void eliminarInterno(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un funcionario.");
        }

        Funcionario funcionario = funcionarioRepository.buscarPorId(id.trim());
        if (funcionario == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }

        // Eliminar reservas del funcionario (copiar lista para evitar ConcurrentModificationException)
        List<model.Reserva> reservasDelFuncionario = reservaRepository.listar().stream()
                .filter(r -> id.trim().equals(r.getFuncionarioId()))
                .collect(java.util.stream.Collectors.toList());
        reservasDelFuncionario.forEach(reservaRepository::eliminar);

        funcionarioRepository.eliminar(id.trim());
        usuarioRepository.eliminar(id.trim());
    }

    private void validarDatos(String id, String nombre, String telefono) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID no puede estar vacío.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío.");
        }
    }
}
