package cn.edu.hnust.easyweibo.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String passwordHash,
        String displayName,
        String major,
        String bio,
        String avatarUrl,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
