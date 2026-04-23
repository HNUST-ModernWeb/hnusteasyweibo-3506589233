package cn.edu.hnust.easyweibo.repository;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;

final class JdbcSupport {
    private JdbcSupport() {
    }

    static PreparedStatement preparedStatementWithKeys(java.sql.Connection connection, String sql, Object... args)
            throws java.sql.SQLException {
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int index = 0; index < args.length; index++) {
            statement.setObject(index + 1, args[index]);
        }
        return statement;
    }

    static long generatedId(KeyHolder keyHolder) {
        if (!keyHolder.getKeyList().isEmpty()) {
            Object id = keyHolder.getKeyList().getFirst().get("id");
            if (id instanceof Number number) {
                return number.longValue();
            }
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Insert did not return a generated key");
        }
        return key.longValue();
    }

    static KeyHolder keyHolder() {
        return new GeneratedKeyHolder();
    }
}
