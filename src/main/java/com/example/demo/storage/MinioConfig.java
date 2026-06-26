package com.example.demo.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

	@Bean
	public MinioClient minioClient(MinioProperties props) throws Exception {
		MinioClient client = MinioClient.builder()
				.endpoint(props.getUrl())
				.credentials(props.getAccessKey(), props.getSecretKey())
				.build();

		// ako docker-init nije odradio (npr. lokalni MinIO bez init kontejnera)
		boolean exists = client.bucketExists(BucketExistsArgs.builder()
				.bucket(props.getBucket())
				.build());
		if (!exists) {
			client.makeBucket(MakeBucketArgs.builder()
					.bucket(props.getBucket())
					.build());
		}
		return client;
	}
}
