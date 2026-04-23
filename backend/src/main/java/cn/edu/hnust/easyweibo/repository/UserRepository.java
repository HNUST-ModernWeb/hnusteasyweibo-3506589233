package cn.edu.hnust.easyweibo.repository;

import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.model.UserStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> mapper = this::mapUser;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User create(String username, String passwordHash, String displayName, String major, String bio) {
        String sql = """
                INSERT INTO users (username, password_hash, display_name, major, bio)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = JdbcSupport.keyHolder();
        jdbcTemplate.update(connection -> JdbcSupport.preparedStatementWithKeys(
                connection,
                sql,
                username,
                passwordHash,
                displayName,
                major,
                bio
        ), keyHolder);
        return findById(JdbcSupport.generatedId(keyHolder)).orElseThrow();
    }

    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM users WHERE username = ?", mapper, username).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", mapper, id).stream().findFirst();
    }

    public User updateProfile(Long userId, String displayName, String major, String bio, String avatarUrl) {
        jdbcTemplate.update("""
                UPDATE users
                SET display_name = ?, major = ?, bio = ?, avatar_url = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, displayName, major, bio, avatarUrl, userId);
        return findById(userId).orElseThrow();
    }

    public UserStats stats(Long userId) {
        Long posts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts WHERE user_id = ?", Long.class, userId);
        Long likes = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM post_likes pl
                JOIN posts p ON p.id = pl.post_id
                WHERE p.user_id = ?
                """, Long.class, userId);
        Long comments = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments WHERE user_id = ?", Long.class, userId);
        return new UserStats(nullToZero(posts), nullToZero(likes), nullToZero(comments));
    }

    private User mapUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("display_name"),
                rs.getString("major"),
                rs.getString("bio"),
                rs.getString("avatar_url"),
                rs.getString("role"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    private long nullToZero(Long value) {
        return value == null ? 0 : value;
    }
}
