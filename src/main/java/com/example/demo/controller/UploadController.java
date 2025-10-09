package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final Path uploadRoot = Paths.get("uploads");

    @PostMapping
    public ResponseEntity<List<String>> upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        if (Files.notExists(uploadRoot)) {
            Files.createDirectories(uploadRoot);
        }
        List<String> publicPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String original = file.getOriginalFilename();
            String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID() + ext;
            Path target = uploadRoot.resolve(filename);
            Files.copy(file.getInputStream(), target);
            publicPaths.add("/uploads/" + filename);
        }
        return ResponseEntity.ok(publicPaths);
    }
}


