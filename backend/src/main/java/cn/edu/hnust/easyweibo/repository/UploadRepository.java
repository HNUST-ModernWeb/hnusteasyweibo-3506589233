package cn.edu.hnust.easyweibo.repository;

import cn.edu.hnust.easyweibo.dto.UploadResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UploadRepository {
    private final JdbcTemplate jdbcTemplate;

    public UploadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UploadResponse save(
            Long userId,
            String originalFilename,
            String storedFilename,
            String contentType,
            long sizeBytes,
            String url
    ) {
        KeyHolder keyHolder = JdbcSupport.keyHolder();
        jdbcTemplate.update(connection -> JdbcSupport.preparedStatementWithKeys(
                connection,
                """
                        INSERT INTO uploads (user_id, original_filename, stored_filename, content_type, size_bytes, url)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                userId,
                originalFilename,
                storedFilename,
                contentType,
                sizeBytes,
                url
        ), keyHolder);
        return new UploadResponse(JdbcSupport.generatedId(keyHolder), url, originalFilename, contentType, sizeBytes);
    }
}
