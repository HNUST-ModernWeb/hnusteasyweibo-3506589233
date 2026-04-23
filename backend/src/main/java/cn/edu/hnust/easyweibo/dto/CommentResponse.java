package cn.edu.hnust.easyweibo.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long userId,
        String authorName,
        String authorAvatar,
        String content,
        LocalDateTime createdAt
) {
}
