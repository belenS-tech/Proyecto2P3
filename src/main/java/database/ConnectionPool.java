package database;

import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

/** Pool pequeño sin dependencias adicionales. close() devuelve el préstamo. */
final class ConnectionPool {
    @FunctionalInterface interface Factory { Connection open() throws SQLException; }
    private final ArrayDeque<Connection> idle = new ArrayDeque<>();
    private final Factory factory;
    private final int maximum;
    private int total;
    ConnectionPool(int maximum, Factory factory) { this.maximum = maximum; this.factory = factory; }
    Connection borrow() throws SQLException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        Connection physical = null;
        synchronized (this) {
            while (physical == null) {
                if (!idle.isEmpty()) {
                    Connection candidate = idle.removeFirst();
                    try { if (!candidate.isClosed() && candidate.isValid(2)) physical = candidate; else discard(candidate); }
                    catch (SQLException e) { discard(candidate); }
                } else if (total < maximum) {
                    total++;
                    try { physical = factory.open(); }
                    catch (SQLException e) { total--; notifyAll(); throw e; }
                } else {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) throw new SQLException("Tiempo de espera agotado para una conexión Oracle.");
                    try { TimeUnit.NANOSECONDS.timedWait(this, remaining); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new SQLException("Conexión interrumpida.",e); }
                }
            }
        }
        Connection target = physical;
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},new InvocationHandler() {
            private boolean returned;
            @Override public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("close")) {
                    if (!returned) { returned = true; release(target); }
                    return null;
                }
                if (method.getName().equals("isClosed")) return returned || target.isClosed();
                if (returned) throw new SQLException("La conexión ya fue devuelta al pool.");
                try { return method.invoke(target,args); }
                catch (InvocationTargetException e) { throw e.getCause(); }
            }
        });
    }
    private synchronized void release(Connection c) {
        try {
            if (!c.getAutoCommit()) { c.rollback(); c.setAutoCommit(true); }
            if (c.isReadOnly()) c.setReadOnly(false);
            c.clearWarnings();
            if (c.isClosed()) discard(c); else idle.addLast(c);
        } catch (SQLException e) { discard(c); }
        notifyAll();
    }
    private void discard(Connection c) {
        try { c.close(); } catch (SQLException ignored) { }
        total--;
    }
    synchronized void closeIdle() { while (!idle.isEmpty()) discard(idle.removeFirst()); }
}
