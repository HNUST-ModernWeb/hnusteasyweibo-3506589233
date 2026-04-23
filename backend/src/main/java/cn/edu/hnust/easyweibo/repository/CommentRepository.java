package cn.edu.hnust.easyweibo.repository;

import cn.edu.hnust.easyweibo.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CommentRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Comment> mapper = this::mapComment;

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Comment create(Long postId, Long userId, String content) {
        KeyHolder keyHolder = JdbcSupport.keyHolder();
        jdbcTemplate.update(connection -> JdbcSupport.preparedStatementWithKeys(
                connection,
                "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)",
                postId,
                userId,
                content
        ), keyHolder);
        return findById(JdbcSupport.generatedId(keyHolder));
    }

    public List<Comment> findByPostId(Long postId) {
        return jdbcTemplate.query("""
                SELECT c.*, u.display_name AS author_name, u.avatar_url AS author_avatar
                FROM comments c
                JOIN users u ON u.id = c.user_id
                WHERE c.post_id = ?
                ORDER BY c.created_at ASC, c.id ASC
                """, mapper, postId);
    }

    private Comment findById(Long id) {
        return jdbcTemplate.query("""
                SELECT c.*, u.display_name AS author_name, u.avatar_url AS author_avatar
                FROM comments c
                JOIN users u ON u.id = c.user_id
                WHERE c.id = ?
                """, mapper, id).stream().findFirst().orElseThrow();
    }

    private Comment mapComment(ResultSet rs, int rowNum) throws SQLException {
        return new Comment(
                rs.getLong("id"),
                rs.getLong("post_id"),
                rs.getLong("user_id"),
                rs.getString("author_name"),
                rs.getString("author_avatar"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
