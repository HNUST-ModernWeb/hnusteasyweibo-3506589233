package cn.edu.hnust.easyweibo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LikeRepository {
    private final JdbcTemplate jdbcTemplate;

    public LikeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean toggle(Long postId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post_likes WHERE post_id = ? AND user_id = ?",
                Integer.class,
                postId,
                userId
        );

        if (count != null && count > 0) {
            jdbcTemplate.update("DELETE FROM post_likes WHERE post_id = ? AND user_id = ?", postId, userId);
            return false;
        }

        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id) VALUES (?, ?)", postId, userId);
        return true;
    }
}
