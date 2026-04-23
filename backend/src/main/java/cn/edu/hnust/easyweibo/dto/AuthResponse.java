package cn.edu.hnust.easyweibo.dto;

public record AuthResponse(
        String tokenType,
        String token,
        UserResponse user
) {
}
