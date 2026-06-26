package com.example.demo.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {

	@Value("${elasticsearch.uris}")
	private String esUri;

	@Bean
	public ElasticsearchClient elasticsearchClient() {
		String stripped = esUri.replace("http://", "").replace("https://", "");
		String[] parts = stripped.split(":");
		String host = parts[0];
		int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;

		RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		return new ElasticsearchClient(transport);
	}
}