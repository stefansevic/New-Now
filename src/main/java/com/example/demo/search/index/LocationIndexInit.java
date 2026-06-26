package com.example.demo.search.index;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Component
public class LocationIndexInit implements ApplicationRunner {

	public static final String INDEX = "locations";

	private final ElasticsearchClient client;

	public LocationIndexInit(ElasticsearchClient client) {
		this.client = client;
	}

	// custom analyzer: cirilica -> latinica (mapping), pa lowercase + asciifolding
	// (c, c, s, z, d posle foldovanja — pretraga je nezavisna od pisma i dijakritika)
	private static final String SETTINGS = """
			{
			  "settings": {
			    "analysis": {
			      "char_filter": {
			        "cyr_to_lat": {
			          "type": "mapping",
			          "mappings": [
			            "А=>A","Б=>B","В=>V","Г=>G","Д=>D","Ђ=>Đ",
			            "Е=>E","Ж=>Ž","З=>Z","И=>I","Ј=>J","К=>K",
			            "Л=>L","Љ=>Lj","М=>M","Н=>N","Њ=>Nj","О=>O",
			            "П=>P","Р=>R","С=>S","Т=>T","Ћ=>Ć","У=>U",
			            "Ф=>F","Х=>H","Ц=>C","Ч=>Č","Џ=>Dž","Ш=>Š",
			            "а=>a","б=>b","в=>v","г=>g","д=>d","ђ=>đ",
			            "е=>e","ж=>ž","з=>z","и=>i","ј=>j","к=>k",
			            "л=>l","љ=>lj","м=>m","н=>n","њ=>nj","о=>o",
			            "п=>p","р=>r","с=>s","т=>t","ћ=>ć","у=>u",
			            "ф=>f","х=>h","ц=>c","ч=>č","џ=>dž","ш=>š"
			          ]
			        }
			      },
			      "analyzer": {
			        "sr_lat_fold": {
			          "type": "custom",
			          "char_filter": ["cyr_to_lat"],
			          "tokenizer": "standard",
			          "filter": ["lowercase", "asciifolding"]
			        }
			      }
			    }
			  },
			  "mappings": {
			    "properties": {
			      "name": {
			        "type": "text",
			        "analyzer": "sr_lat_fold",
			        "fields": {
			          "keyword": { "type": "keyword" }
			        }
			      },
			      "descriptionUi":  { "type": "text", "analyzer": "sr_lat_fold" },
			      "descriptionPdf": { "type": "text", "analyzer": "sr_lat_fold" },
			      "reviewCount":    { "type": "integer" },
			      "avgPerformance": { "type": "float" },
			      "avgSoundLight":  { "type": "float" },
			      "avgVenue":       { "type": "float" },
			      "avgOverall":     { "type": "float" },
			      "avgTotal":       { "type": "float" },
			      "pdfKey":         { "type": "keyword" }
			    }
			  }
			}
			""";

	@Override
	public void run(ApplicationArguments args) throws Exception {
		boolean exists = client.indices().exists(b -> b.index(INDEX)).value();
		if (exists) return;

		var in = new ByteArrayInputStream(SETTINGS.getBytes(StandardCharsets.UTF_8));
		client.indices().create(CreateIndexRequest.of(b -> b.index(INDEX).withJson(in)));
		System.out.println("Created ES index: " + INDEX);
	}
}