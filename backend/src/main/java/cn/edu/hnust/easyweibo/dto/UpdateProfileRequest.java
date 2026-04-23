package cn.edu.hnust.easyweibo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 40) String displayName,
        @Size(max = 80) String major,
        @Size(max = 240) String bio,
        @Size(max = 500) String avatarUrl
) {
}
