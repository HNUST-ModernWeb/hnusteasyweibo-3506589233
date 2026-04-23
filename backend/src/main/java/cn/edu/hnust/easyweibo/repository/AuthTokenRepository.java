package cn.edu.hnust.easyweibo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class AuthTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public AuthTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String token, Long userId, LocalDateTime expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO auth_tokens (token, user_id, expires_at)
                VALUES (?, ?, ?)
                """, token, userId, expiresAt);
    }

    public Optional<Long> findActiveUserId(String token) {
        return jdbcTemplate.query("""
                SELECT user_id
                FROM auth_tokens
                WHERE token = ? AND revoked = FALSE AND expires_at > CURRENT_TIMESTAMP
                """, (rs, rowNum) -> rs.getLong("user_id"), token).stream().findFirst();
    }

    public void revoke(String token) {
        jdbcTemplate.update("UPDATE auth_tokens SET revoked = TRUE WHERE token = ?", token);
    }
}
