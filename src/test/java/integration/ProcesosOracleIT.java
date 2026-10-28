package integration;

import client.ServiceProxy;
import controller.AlertasController;
import database.Database;
import model.*;
import network.Protocol.Alert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import repository.Repositories;
import view.AlertasPanel;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/** JAR backend y dos clientes en JVM independientes, con Oracle real y limpieza aislada. */
@EnabledIfSystemProperty(named="oracle.it", matches="true")
@Timeout(180)
class ProcesosOracleIT {
    @TempDir Path temporal;
    final String clave = "E2E-"+UUID.randomUUID();
    final String adminId = clave+"-admin", f1 = clave+"-f1", f2 = clave+"-f2";
    final String pass1 = clave+"-clave1", pass2 = clave+"-clave2";
    final String r1 = clave+"-r1", r2 = clave+"-r2", r3 = clave+"-r3";
    final LocalDate dia = LocalDate.of(2040,1,2);
    final List<Process> clientes = new ArrayList<>();
    Process backend;
    int puerto, arranque;

    String java() { return Path.of(System.getProperty("java.home"),"bin","java.exe").toString(); }
    String dependencias(boolean cliente) {
        return Arrays.stream(System.getProperty("surefire.test.class.path",System.getProperty("java.class.path"))
                .split(Pattern.quote(java.io.File.pathSeparator)))
                .filter(s -> s.endsWith(".jar") && Files.isRegularFile(Path.of(s)))
                .filter(s -> !Path.of(s).toAbsolutePath().normalize().startsWith(Path.of("target").toAbsolutePath().normalize()))
                .filter(s -> !cliente || !s.contains("ojdbc"))
                .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
    }
    String classpath(boolean cliente) {
        Path jar = Path.of("target","ProyectoP3-1.0-SNAPSHOT-"+(cliente?"frontend":"backend")+".jar").toAbsolutePath();
        assertTrue(Files.isRegularFile(jar),"Ejecutar mvn verify para generar los JAR primero");
        return (cliente ? Path.of("target/test-classes").toAbsolutePath()+java.io.File.pathSeparator : "")
                +jar+java.io.File.pathSeparator+dependencias(cliente);
    }
    void iniciarBackend() throws Exception {
        Path log = temporal.resolve("backend-"+(++arranque)+".log");
        List<String> comando = new ArrayList<>(List.of(java(),"-Dserver.port=0","-cp",classpath(false)));
        Path configuracion = Path.of(System.getProperty("db.config","database.properties")).toAbsolutePath();
        if (Files.isRegularFile(configuracion)) comando.add(1,"-Ddb.config="+configuracion);
        comando.add("server.BackendMain");
        backend = new ProcessBuilder(comando).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        long limite = System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
        var expresion = Pattern.compile("escuchando en el puerto (\\d+)");
        while (System.nanoTime()<limite) {
            if (Files.isRegularFile(log)) {
                var coincidencia = expresion.matcher(Files.readString(log));
                if (coincidencia.find()) { puerto = Integer.parseInt(coincidencia.group(1)); return; }
            }
            assertTrue(backend.isAlive(),"El backend terminó: "+Files.readString(log));
            Thread.sleep(30);
        }
        fail("El backend no inició dentro del plazo");
    }
    void detenerBackend() throws Exception {
        if (backend != null && backend.isAlive()) {
            backend.destroy();
            if (!backend.waitFor(10,TimeUnit.SECONDS)) { backend.destroyForcibly(); backend.waitFor(10,TimeUnit.SECONDS); }
        }
    }
    ServiceProxy conectar(String id,String password) throws Exception {
        var proxy = new ServiceProxy("127.0.0.1",puerto);
        proxy.service(api.AuthService.class).iniciarSesion(id,password); return proxy;
    }
    Process cliente(String modo,String usuario,String password,Path salida,String... argumentos) throws Exception {
        List<String> comando = new ArrayList<>(List.of(java(),"-cp",classpath(true),"integration.ClienteDistribuido",
                modo,"127.0.0.1",String.valueOf(puerto),usuario,password,salida.toString()));
        comando.addAll(List.of(argumentos));
        var proceso = new ProcessBuilder(comando).redirectErrorStream(true)
                .redirectOutput(temporal.resolve(salida.getFileName()+".log").toFile());
        for (String variable : List.of("DB_URL","DB_USER","DB_PASSWORD","GEMINI_API_KEY")) proceso.environment().remove(variable);
        Process iniciado = proceso.start(); clientes.add(iniciado); return iniciado;
    }
    Properties resultado(Process proceso,Path salida) throws Exception {
        assertTrue(proceso.waitFor(30,TimeUnit.SECONDS),"El cliente no terminó");
        assertEquals(0,proceso.exitValue(),"Cliente falló: "+Files.readString(temporal.resolve(salida.getFileName()+".log")));
        Properties p = new Properties(); try(var in=Files.newInputStream(salida)) { p.load(in); } return p;
    }
    List<Properties> carrera(String categoria,String fase) throws Exception {
        Path barrera=temporal.resolve(fase+"-inicio"), a=temporal.resolve(fase+"-a"), b=temporal.resolve(fase+"-b");
        Path pa=temporal.resolve(fase+"-a.properties"), pb=temporal.resolve(fase+"-b.properties");
        Process ca=cliente("reservar",f1,pass1,pa,a.toString(),barrera.toString(),categoria,clave+fase+"-a");
        Process cb=cliente("reservar",f2,pass2,pb,b.toString(),barrera.toString(),categoria,clave+fase+"-b");
        long limite=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
        while (!Files.exists(a) || !Files.exists(b)) {
            assertTrue(ca.isAlive() && cb.isAlive(),() -> {
                try { return "Un cliente terminó antes de la barrera: "
                        +Files.readString(temporal.resolve(pa.getFileName()+".log"))
                        +Files.readString(temporal.resolve(pb.getFileName()+".log")); }
                catch(Exception error) { return error.toString(); }
            });
            if (System.nanoTime()>limite) fail("Los clientes no se prepararon");
            Thread.sleep(30);
        }
        Files.writeString(barrera,"INICIAR"); return List.of(resultado(ca,pa),resultado(cb,pb));
    }
    static class AlertasPrueba extends AlertasPanel {
        boolean confirmar;
        Reserva detalle;
        @Override public boolean confirmarDescarte() { return confirmar; }
        @Override public void mostrarDetalle(Reserva reserva) { detalle=reserva; }
    }
    static JList<?> lista(Component componente) {
        if (componente instanceof JList<?> l) return l;
        if (componente instanceof Container c) for (Component hijo : c.getComponents()) {
            JList<?> l=lista(hijo); if(l!=null) return l;
        }
        return null;
    }
    void limpiar() {
        Database.transaction(c -> {
            String[] sql={"DELETE FROM p3_reservas WHERE funcionario_id IN (?,?)",
                    "DELETE FROM p3_recursos WHERE id IN (?,?,?)",
                    "DELETE FROM p3_categorias WHERE descripcion IN (?,?,?)",
                    "DELETE FROM p3_funcionarios WHERE id IN (?,?)",
                    "DELETE FROM p3_usuarios WHERE id IN (?,?,?)"};
            String[][] parametros={{f1,f2},{r1,r2,r3},{clave+"-dos",clave+"-dos-actualizada",clave+"-uno"},{f1,f2},{adminId,f1,f2}};
            for(int i=0;i<sql.length;i++) try(var sentencia=c.prepareStatement(sql[i])) {
                for(int j=0;j<parametros[i].length;j++) sentencia.setString(j+1,parametros[i][j]); sentencia.executeUpdate();
            }
            return null;
        });
    }

    @Test void dosClientesSeparadosReservanAlertanYConservanDatosTrasReinicio() throws Exception {
        ServiceProxy admin=null;
        AlertasController controlador=null;
        Repositories.usuarios().guardar(new Usuario(adminId,"prueba",Rol.ADMINISTRADOR));
        try {
            iniciarBackend(); admin=conectar(adminId,"prueba");
            var personas=admin.service(api.FuncionarioService.class);
            var categorias=admin.service(api.CategoriaService.class);
            var recursos=admin.service(api.RecursoService.class);
            personas.incluir(f1,"Funcionario E2E uno","88888888"); personas.incluir(f2,"Funcionario E2E dos","77777777");
            personas.modificar(f1,"Funcionario E2E actualizado","66666666");
            assertEquals("Funcionario E2E actualizado",personas.buscar(f1,"").getFirst().getNombre());
            for(String id:List.of(f1,f2)) try(var p=conectar(id,id)) {
                p.service(api.AuthService.class).cambiarClave(id,id.equals(f1)?pass1:pass2,id.equals(f1)?pass1:pass2);
            }
            categorias.incluir(clave+"-dos"); CategoriaRecurso dos=categorias.buscar(clave+"-dos").getFirst();
            categorias.modificar(dos.getId(),clave+"-dos-actualizada"); dos=categorias.buscarPorId(dos.getId());
            categorias.incluir(clave+"-uno"); CategoriaRecurso uno=categorias.buscar(clave+"-uno").getFirst();
            recursos.registrar(new Recurso(r1,dos,"Sala E2E uno")); recursos.registrar(new Recurso(r2,dos,"Sala E2E dos"));
            recursos.registrar(new Recurso(r3,uno,"Sala E2E única"));
            recursos.actualizar(new Recurso(r1,dos,"Sala E2E modificada"));
            assertEquals("Sala E2E modificada",recursos.buscarPorId(r1).getDescripcion());
            AlertasPrueba panel=new AlertasPrueba(); controlador=new AlertasController(panel,admin);
            BlockingQueue<Alert> avisos=new LinkedBlockingQueue<>(); admin.addAlertListener(avisos::add);
            System.out.println("E2E: backend JVM separado, CRUD y cambio de clave verificados.");

            var dosResultados=carrera(dos.getId(),"doble");
            assertTrue(dosResultados.stream().allMatch(p -> p.getProperty("estado").equals("OK")));
            assertEquals(2,dosResultados.stream().map(p -> p.getProperty("reserva")).distinct().count());
            assertEquals(2,dosResultados.stream().map(p -> p.getProperty("recurso")).distinct().count());
            assertTrue(dosResultados.stream().allMatch(p -> p.getProperty("alertas").equals("1")));
            assertNotNull(avisos.poll(5,TimeUnit.SECONDS)); assertNotNull(avisos.poll(5,TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                JList<?> listado=lista(panel); assertNotNull(listado); assertEquals(2,listado.getModel().getSize());
                listado.setSelectedIndex(0); panel.ver().doClick(0); assertNotNull(panel.detalle);
                panel.confirmar=false; panel.descartar().doClick(0); assertEquals(2,listado.getModel().getSize());
                panel.confirmar=true; panel.descartar().doClick(0); assertEquals(1,listado.getModel().getSize());
            });
            assertNotNull(admin.service(api.ReservaService.class).buscarPorId(panel.detalle.getId()),"Descartar no elimina en Oracle");
            var unResultado=carrera(uno.getId(),"unica");
            assertEquals(1,unResultado.stream().filter(p -> p.getProperty("estado").equals("OK")).count());
            assertEquals(1,unResultado.stream().filter(p -> p.getProperty("estado").equals("SIN_DISPONIBILIDAD")).count());
            assertNotNull(avisos.poll(5,TimeUnit.SECONDS)); assertNull(avisos.poll(100,TimeUnit.MILLISECONDS));
            try(var p=conectar(f1,pass1)) {
                var reservas=p.service(api.ReservaService.class); int antes=reservas.listarTodos().size();
                Reserva parcial=new Reserva("",clave+"-parcial",dia,LocalTime.of(10,45),LocalTime.of(11,17),f1,EstadoReserva.ACTIVA);
                parcial.setDetalles(List.of(new DetalleReserva(dos,null),new DetalleReserva(uno,null)));
                assertThrows(IllegalArgumentException.class,() -> reservas.registrar(parcial));
                assertEquals(antes,reservas.listarTodos().size());
                assertThrows(IllegalArgumentException.class,() -> p.service(api.FuncionarioService.class).listarTodos());
                assertThrows(IllegalArgumentException.class,() -> p.service(api.RecursoService.class).listarTodos());
            }
            controlador.close(); controlador=null; admin.logout(); admin.service(api.AuthService.class).iniciarSesion(adminId,"prueba");
            AlertasPrueba nueva=new AlertasPrueba();
            try(var siguiente=new AlertasController(nueva,admin)) {
                SwingUtilities.invokeAndWait(() -> assertEquals(0,lista(nueva).getModel().getSize()));
            }
            String persistida=dosResultados.getFirst().getProperty("reserva");
            admin.close(); admin=null; detenerBackend(); iniciarBackend();
            Path verificacion=temporal.resolve("persistencia.properties");
            Properties persistencia=resultado(cliente("verificar",f1,pass1,verificacion,persistida),verificacion);
            assertEquals("PERSISTE",persistencia.getProperty("estado"));
            try(var p=conectar(f1,pass1)) {
                var reservas=p.service(api.ReservaService.class); Reserva guardada=reservas.buscarPorId(persistida);
                String liberado=guardada.getDetalles().getFirst().getRecurso().getId(); reservas.eliminar(guardada);
                assertNull(reservas.buscarPorId(persistida));
                assertEquals(liberado,p.service(api.DisponibilidadService.class)
                        .buscarRecursoDisponible(dos,dia,LocalTime.of(10,45),LocalTime.of(11,17)).getId());
            }
            System.out.println("E2E: concurrencia de JVM, alertas Swing, descarte local, sesión, persistencia y liberación verificadas.");
        } finally {
            if(controlador!=null) controlador.close(); if(admin!=null) admin.close();
            for(Process p:clientes) if(p.isAlive()) { p.destroyForcibly(); p.waitFor(10,TimeUnit.SECONDS); }
            try { detenerBackend(); } finally { limpiar(); }
        }
    }
}
