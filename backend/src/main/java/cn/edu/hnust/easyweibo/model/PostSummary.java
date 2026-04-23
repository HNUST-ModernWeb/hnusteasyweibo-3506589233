package cn.edu.hnust.easyweibo.model;

import java.time.LocalDateTime;

public record PostSummary(
        Long id,
        Long userId,
        String authorName,
        String authorMajor,
        String authorAvatar,
        String content,
        String topic,
        String visibility,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int likeCount,
        int commentCount,
        boolean likedByCurrentUser
) {
}
