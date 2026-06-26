package com.example.demo.search.controller;

import com.example.demo.search.dto.SearchRequest;
import com.example.demo.search.dto.SearchResult;
import com.example.demo.search.service.LocationIndexer;
import com.example.demo.search.service.LocationSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search/locations")
public class LocationSearchController {

	private final LocationIndexer indexer;
	private final LocationSearchService searchService;

	public LocationSearchController(LocationIndexer indexer, LocationSearchService searchService) {
		this.indexer = indexer;
		this.searchService = searchService;
	}

	@PostMapping
	public ResponseEntity<List<SearchResult>> search(@RequestBody SearchRequest req) throws Exception {
		return ResponseEntity.ok(searchService.search(req));
	}

	@GetMapping("/{name}/similar")
	public ResponseEntity<List<SearchResult>> similar(@PathVariable String name) throws Exception {
		return ResponseEntity.ok(searchService.moreLikeThis(name));
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
