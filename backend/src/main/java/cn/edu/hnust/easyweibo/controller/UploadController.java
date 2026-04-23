package cn.edu.hnust.easyweibo.controller;

import cn.edu.hnust.easyweibo.config.AuthSupport;
import cn.edu.hnust.easyweibo.dto.UploadResponse;
import cn.edu.hnust.easyweibo.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResponse upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return uploadService.upload(AuthSupport.requireUser(request), file);
    }
}
