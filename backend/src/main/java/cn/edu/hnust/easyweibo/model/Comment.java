package cn.edu.hnust.easyweibo.model;

import java.time.LocalDateTime;

public record Comment(
        Long id,
        Long postId,
        Long userId,
        String authorName,
        String authorAvatar,
        String content,
        LocalDateTime createdAt
) {
}
