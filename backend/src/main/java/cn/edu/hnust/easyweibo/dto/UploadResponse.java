package cn.edu.hnust.easyweibo.dto;

public record UploadResponse(
        Long id,
        String url,
        String originalFilename,
        String contentType,
        long sizeBytes
) {
}
