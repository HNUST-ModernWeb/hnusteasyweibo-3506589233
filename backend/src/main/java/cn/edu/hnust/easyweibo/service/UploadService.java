package cn.edu.hnust.easyweibo.service;

import cn.edu.hnust.easyweibo.dto.UploadResponse;
import cn.edu.hnust.easyweibo.exception.ApiException;
import cn.edu.hnust.easyweibo.model.User;
import cn.edu.hnust.easyweibo.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
public class UploadService {
    private final UploadRepository uploadRepository;
    private final Path uploadDir;
    private final long maxSizeBytes;

    public UploadService(
            UploadRepository uploadRepository,
            @Value("${app.uploads.dir:uploads}") String uploadDir,
            @Value("${app.uploads.max-size-bytes:5242880}") long maxSizeBytes
    ) {
        this.uploadRepository = uploadRepository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
    }

    public UploadResponse upload(User currentUser, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "请选择要上传的图片");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD_TOO_LARGE", "图片大小不能超过 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "只允许上传图片文件");
        }

        String originalFilename = safeFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + extension(originalFilename, contentType);
        Path target = uploadDir.resolve(storedFilename).normalize();

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(target);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_FAILED", "图片保存失败");
        }

        String url = "/uploads/" + storedFilename;
        return uploadRepository.save(currentUser.id(), originalFilename, storedFilename, contentType, file.getSize(), url);
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "upload";
        }
        return Path.of(filename).getFileName().toString();
    }

    private String extension(String filename, String contentType) {
        int index = filename.lastIndexOf('.');
        if (index >= 0 && index < filename.length() - 1) {
            return filename.substring(index).toLowerCase(Locale.ROOT);
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
