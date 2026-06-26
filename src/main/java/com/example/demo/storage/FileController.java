package com.example.demo.storage;

import io.minio.StatObjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

	private final StorageService storage;

	public FileController(StorageService storage) {
		this.storage = storage;
	}

	@PostMapping
	public ResponseEntity<List<String>> upload(@RequestParam("files") List<MultipartFile> files,
			@RequestParam(value = "type", defaultValue = "image") String type) throws Exception {
		String prefix = "pdf".equalsIgnoreCase(type) ? "pdfs" : "images";
		List<String> urls = new ArrayList<>();
		for (MultipartFile f : files) {
			if (f.isEmpty()) continue;
			String key = storage.upload(f.getInputStream(), f.getSize(), f.getContentType(), prefix, f.getOriginalFilename());
			urls.add("/api/files/" + key);
		}
		return ResponseEntity.ok(urls);
	}

	@GetMapping("/**")
	public ResponseEntity<InputStreamResource> download(HttpServletRequest req) throws Exception {
		String key = req.getRequestURI().substring("/api/files/".length());
		StatObjectResponse stat = storage.stat(key);
		InputStream in = storage.download(key);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(stat.contentType()))
				.contentLength(stat.size())
				.header(HttpHeaders.CACHE_CONTROL, "max-age=3600, public")
				.body(new InputStreamResource(in));
	}
}
