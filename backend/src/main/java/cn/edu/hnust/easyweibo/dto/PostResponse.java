package cn.edu.hnust.easyweibo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        Long userId,
        String authorName,
        String authorMajor,
        String authorAvatar,
        String content,
        String topic,
        String visibility,
        String imageUrl,
        int likeCount,
        int commentCount,
        boolean likedByCurrentUser,
        List<CommentResponse> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
