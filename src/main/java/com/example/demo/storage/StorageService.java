package com.example.demo.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class StorageService {

	private final MinioClient client;
	private final MinioProperties props;

	public StorageService(MinioClient client, MinioProperties props) {
		this.client = client;
		this.props = props;
	}

	// vraca key (npr. images/<uuid>.jpg)
	public String upload(InputStream in, long size, String contentType, String prefix, String originalFilename) throws Exception {
		String ext = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
		}
		String key = prefix + "/" + UUID.randomUUID() + ext;
		client.putObject(PutObjectArgs.builder()
				.bucket(props.getBucket())
				.object(key)
				.stream(in, size, -1)
				.contentType(contentType != null ? contentType : "application/octet-stream")
				.build());
		return key;
	}

	public InputStream download(String key) throws Exception {
		return client.getObject(GetObjectArgs.builder()
				.bucket(props.getBucket())
				.object(key)
				.build());
	}

	public StatObjectResponse stat(String key) throws Exception {
		return client.statObject(StatObjectArgs.builder()
				.bucket(props.getBucket())
				.object(key)
				.build());
	}

	public void delete(String key) {
		if (key == null || key.isBlank()) return;
		try {
			client.removeObject(RemoveObjectArgs.builder()
					.bucket(props.getBucket())
					.object(key)
					.build());
		} catch (Exception e) {
			System.err.println("Failed to delete object: " + key + " (" + e.getMessage() + ")");
		}
	}

	// pomocna: ako stigne pun URL /api/files/<key>, vrati samo <key>
	public static String stripUrlPrefix(String pathOrKey) {
		if (pathOrKey == null) return null;
		String prefix = "/api/files/";
		if (pathOrKey.startsWith(prefix)) {
			return pathOrKey.substring(prefix.length());
		}
		return pathOrKey;
	}
}
