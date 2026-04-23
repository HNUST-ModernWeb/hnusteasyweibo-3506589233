package cn.edu.hnust.easyweibo.repository;

import cn.edu.hnust.easyweibo.model.Post;
import cn.edu.hnust.easyweibo.model.PostSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PostRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<PostSummary> summaryMapper = this::mapSummary;
    private final RowMapper<Post> postMapper = this::mapPost;

    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PostSummary> findAll(String topic, String keyword, int page, int size, Long currentUserId) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseSummarySql(currentUserId));
        sql.append(" WHERE 1 = 1");

        if (topic != null && !topic.isBlank()) {
            sql.append(" AND p.topic = ?");
            params.add(topic.trim());
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND p.content LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }

        sql.append(" ORDER BY p.created_at DESC, p.id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), summaryMapper, params.toArray());
    }

    public Optional<PostSummary> findSummaryById(Long id, Long currentUserId) {
        String sql = baseSummarySql(currentUserId) + " WHERE p.id = ?";
        return jdbcTemplate.query(sql, summaryMapper, id).stream().findFirst();
    }

    public Optional<Post> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM posts WHERE id = ?", postMapper, id).stream().findFirst();
    }

    public Post create(Long userId, String content, String topic, String visibility, String imageUrl) {
        KeyHolder keyHolder = JdbcSupport.keyHolder();
        jdbcTemplate.update(connection -> JdbcSupport.preparedStatementWithKeys(
                connection,
                """
                        INSERT INTO posts (user_id, content, topic, visibility, image_url)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                userId,
                content,
                topic,
                visibility,
                imageUrl
        ), keyHolder);
        return findById(JdbcSupport.generatedId(keyHolder)).orElseThrow();
    }

    public void update(Long id, String content, String topic, String visibility, String imageUrl) {
        jdbcTemplate.update("""
                UPDATE posts
                SET content = ?, topic = ?, visibility = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, content, topic, visibility, imageUrl, id);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
    }

    private String baseSummarySql(Long currentUserId) {
        long viewerId = currentUserId == null ? -1L : currentUserId;
        return """
                SELECT p.*,
                       u.display_name AS author_name,
                       u.major AS author_major,
                       u.avatar_url AS author_avatar,
                       (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id) AS like_count,
                       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comment_count,
                       EXISTS (
                           SELECT 1 FROM post_likes viewer_like
                           WHERE viewer_like.post_id = p.id AND viewer_like.user_id = %d
                       ) AS liked_by_current_user
                FROM posts p
                JOIN users u ON u.id = p.user_id
                """.formatted(viewerId);
    }

    private PostSummary mapSummary(ResultSet rs, int rowNum) throws SQLException {
        return new PostSummary(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("author_name"),
                rs.getString("author_major"),
                rs.getString("author_avatar"),
                rs.getString("content"),
                rs.getString("topic"),
                rs.getString("visibility"),
                rs.getString("image_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getInt("like_count"),
                rs.getInt("comment_count"),
                rs.getBoolean("liked_by_current_user")
        );
    }

    private Post mapPost(ResultSet rs, int rowNum) throws SQLException {
        return new Post(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("content"),
                rs.getString("topic"),
                rs.getString("visibility"),
                rs.getString("image_url"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
