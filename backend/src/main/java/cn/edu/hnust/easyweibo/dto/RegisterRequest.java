package cn.edu.hnust.easyweibo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 40) String username,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotBlank @Size(max = 40) String displayName,
        @Size(max = 80) String major,
        @Size(max = 240) String bio
) {
}
