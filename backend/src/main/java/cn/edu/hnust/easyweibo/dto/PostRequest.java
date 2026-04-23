package cn.edu.hnust.easyweibo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank @Size(min = 5, max = 280) String content,
        @NotBlank @Size(max = 40) String topic,
        @NotBlank @Size(max = 40) String visibility,
        @Size(max = 500) String imageUrl
) {
}
