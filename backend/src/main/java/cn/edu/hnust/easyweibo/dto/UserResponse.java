package cn.edu.hnust.easyweibo.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String major,
        String bio,
        String avatarUrl,
        String role,
        long postCount,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {
}
