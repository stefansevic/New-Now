package com.example.demo.controller;

import com.example.demo.model.Location;
import com.example.demo.service.LocationService;
import com.example.demo.repository.ManagesRepository;
import java.time.LocalDate;
import com.example.demo.model.Manages;
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
    private final ManagesRepository managesRepository;

    public LocationController(LocationService locationService, ManagesRepository managesRepository) {
        this.locationService = locationService;
        this.managesRepository = managesRepository;
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
        if (principal == null) return ResponseEntity.status(401).build();
        String email = principal.getUsername();
        boolean isAdmin = email.equals("admin@system.local");
        boolean isManager = managesRepository
                .findByUserEmailAndLocationName(email, name)
                .stream()
                .anyMatch(m -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start = m.getStartDate();
                    LocalDate end = m.getEndDate();
                    boolean started = (start == null) || !start.isAfter(today);
                    boolean notEnded = (end == null) || !end.isBefore(today);
                    return started && notEnded;
                });
        if (!(isAdmin || isManager)) return ResponseEntity.status(403).build();
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

	// A2: Admin-only - add manager to a location
	@PostMapping("/{name}/managers")
	public ResponseEntity<?> addManager(@AuthenticationPrincipal UserDetails principal,
			@PathVariable String name,
			@RequestParam String email,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {
		if (principal == null || !principal.getUsername().equals("admin@system.local")) return ResponseEntity.status(403).build();
		Manages m = new Manages();
		m.setLocation(new Location());
		m.getLocation().setName(name);
		m.setUser(new com.example.demo.model.User());
		m.getUser().setEmail(email);
		m.setStartDate(startDate == null ? LocalDate.now() : LocalDate.parse(startDate));
		m.setEndDate(endDate == null ? null : LocalDate.parse(endDate));
		managesRepository.save(m);
		return ResponseEntity.ok().build();
	}

	// A2: Admin-only - remove manager from a location
	@DeleteMapping("/{name}/managers/{email}")
	public ResponseEntity<?> removeManager(@AuthenticationPrincipal UserDetails principal,
			@PathVariable String name,
			@PathVariable String email) {
		if (principal == null || !principal.getUsername().equals("admin@system.local")) return ResponseEntity.status(403).build();
		managesRepository.deleteByUserEmailAndLocationName(email, name);
		return ResponseEntity.ok().build();
	}

	// Optional: list managers for a location (admin-only)
	@GetMapping("/{name}/managers")
	public ResponseEntity<?> listManagers(@AuthenticationPrincipal UserDetails principal, @PathVariable String name) {
		if (principal == null || !principal.getUsername().equals("admin@system.local")) return ResponseEntity.status(403).build();
		return ResponseEntity.ok(managesRepository.findByLocationName(name));
	}
}


