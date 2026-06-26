package com.example.demo.search.controller;

import com.example.demo.search.service.LocationIndexer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/search/locations")
public class LocationSearchController {

	private final LocationIndexer indexer;

	public LocationSearchController(LocationIndexer indexer) {
		this.indexer = indexer;
	}

	// admin-only, za demo i oporavak indeksa
	@PostMapping("/reindex-all")
	public ResponseEntity<?> reindexAll(@AuthenticationPrincipal UserDetails principal) {
		if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
			return ResponseEntity.status(403).build();
		}
		int count = indexer.reindexAll();
		return ResponseEntity.ok(Map.of("indexed", count));
	}
}