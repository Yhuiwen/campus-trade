package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png");

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String extension = original == null || !original.contains(".")
                ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (file.isEmpty() || !ALLOWED.contains(extension)) throw new BusinessException("仅支持 jpg、jpeg、png 图片");
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        String filename = UUID.randomUUID() + "." + extension;
        file.transferTo(directory.resolve(filename));
        return ApiResponse.success(Map.of("url", "/uploads/" + filename));
    }
}
