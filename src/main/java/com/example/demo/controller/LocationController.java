package com.example.demo.controller;

import com.example.demo.model.Location;
import com.example.demo.service.LocationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

record CreateLocationRequest(String name, String address, String type, String description, List<String> imagePaths) {}
record UpdateLocationRequest(String address, String type, String description) {}

@RestController
@RequestMapping("/api/locations")
public class LocationController {

	private final LocationService locationService;

	public LocationController(LocationService locationService) {
		this.locationService = locationService;
	}

    @PostMapping
    public ResponseEntity<Location> create(@AuthenticationPrincipal UserDetails principal, @RequestBody CreateLocationRequest req) {
        if (principal == null || !principal.getUsername().equals("admin@system.local")) {
            return ResponseEntity.status(403).build();
        }
		Location l = new Location();
		l.setName(req.name());
		l.setAddress(req.address());
		l.setType(req.type());
		l.setDescription(req.description());
		return ResponseEntity.ok(locationService.create(l, req.imagePaths()));
	}

    @PutMapping("/{name}")
    public ResponseEntity<Location> update(@AuthenticationPrincipal UserDetails principal, @PathVariable String name, @RequestBody UpdateLocationRequest req) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(locationService.update(name, req.address(), req.type(), req.description()));
    }

	@GetMapping("/{name}")
    public ResponseEntity<LocationService.LocationDetails> details(@PathVariable String name) {
		return ResponseEntity.ok(locationService.getDetails(name));
	}

	@GetMapping
	public ResponseEntity<List<Location>> list() {
		return ResponseEntity.ok(locationService.listAll());
	}
}


