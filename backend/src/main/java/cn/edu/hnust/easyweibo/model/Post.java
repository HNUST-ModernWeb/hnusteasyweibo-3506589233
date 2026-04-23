package cn.edu.hnust.easyweibo.model;

import java.time.LocalDateTime;

public record Post(
        Long id,
        Long userId,
        String content,
        String topic,
        String visibility,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
