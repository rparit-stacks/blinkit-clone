package com.nainital.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "docs") String folder) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file provided"));
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";

        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, folder);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("File upload failed", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }

        String url = "/uploads/" + folder + "/" + filename;
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }
}
