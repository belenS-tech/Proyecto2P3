package repository;

import database.Database;
import java.sql.*;
import java.util.*;

abstract class JdbcSupport {
    @FunctionalInterface interface Mapper<T> { T map(ResultSet rs) throws SQLException; }

    static int execute(String sql, Object... values) {
        return Database.use(c -> {
            try (PreparedStatement s = c.prepareStatement(sql)) {
                bind(s, values);
                return s.executeUpdate();
            }
        });
    }

    static <T> List<T> query(String sql, Mapper<T> mapper, Object... values) {
        return Database.use(c -> {
            try (PreparedStatement s = c.prepareStatement(sql)) {
                bind(s, values);
                try (ResultSet rs = s.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (rs.next()) result.add(mapper.map(rs));
                    return result;
                }
            }
        });
    }

    private static void bind(PreparedStatement s, Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) s.setObject(i + 1, values[i]);
    }
    static <T> T first(List<T> rows) { return rows.isEmpty() ? null : rows.getFirst(); }
}
