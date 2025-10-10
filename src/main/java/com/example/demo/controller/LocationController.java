package com.example.demo.controller;

import com.example.demo.model.Location;
import com.example.demo.service.LocationService;
import com.example.demo.repository.ManagesRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.Manages;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

record CreateLocationRequest(String name, String address, String type, String description, List<String> imagePaths) {}
record UpdateLocationRequest(String address, String type, String description) {}

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;
    private final ManagesRepository managesRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public LocationController(LocationService locationService, ManagesRepository managesRepository, 
                            LocationRepository locationRepository, UserRepository userRepository) {
        this.locationService = locationService;
        this.managesRepository = managesRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
	}

    @PostMapping
    public ResponseEntity<Location> create(@AuthenticationPrincipal UserDetails principal, @RequestBody CreateLocationRequest req) {
        if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
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
        boolean isAdmin = email.equals("admin@gmail.com");
        boolean isManager = !managesRepository
                .findByUserEmailAndLocationName(email, name)
                .isEmpty();
        if (!(isAdmin || isManager)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(locationService.update(name, req.address(), req.type(), req.description()));
    }

	@GetMapping("/{name}")
    public ResponseEntity<LocationService.LocationDetails> details(@PathVariable String name) {
		return ResponseEntity.ok(locationService.getDetails(name));
	}

	@GetMapping
	public ResponseEntity<List<LocationService.LocationDetails>> list() {
		return ResponseEntity.ok(locationService.listAll());
	}

    @DeleteMapping("/{name}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails principal, @PathVariable String name) {
        if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
            return ResponseEntity.status(403).build();
        }
        locationService.delete(name);
        return ResponseEntity.ok().build();
    }

	// Admin-only - add manager to a location
	@PostMapping("/{name}/managers")
	@Transactional
	public ResponseEntity<?> addManager(@AuthenticationPrincipal UserDetails principal,
			@PathVariable String name,
			@RequestParam String email) {
		if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
			return ResponseEntity.status(403).build();
		}
		
		// Check if user exists
		if (!userRepository.existsById(email)) {
			return ResponseEntity.badRequest().body("User does not exist");
		}
		
		// Check if location exists
		if (!locationRepository.existsById(name)) {
			return ResponseEntity.badRequest().body("Location does not exist");
		}
		
		// Check if user is already a manager for this location
		List<Manages> existingManages = managesRepository.findByUserEmailAndLocationName(email, name);
		if (!existingManages.isEmpty()) {
			return ResponseEntity.badRequest().body("User is already a manager for this location");
		}
		
		Manages m = new Manages();
		Location location = new Location();
		location.setName(name);
		m.setLocation(location);
		
		com.example.demo.model.User user = new com.example.demo.model.User();
		user.setEmail(email);
		m.setUser(user);
		
		managesRepository.save(m);
		return ResponseEntity.ok().build();
	}

	// Admin-only - remove manager from a location
	@DeleteMapping("/{name}/managers/{email}")
	@Transactional
	public ResponseEntity<?> removeManager(@AuthenticationPrincipal UserDetails principal,
			@PathVariable String name,
			@PathVariable String email) {
		if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
			return ResponseEntity.status(403).build();
		}
		
		// Check if the management relationship exists
		List<Manages> manages = managesRepository.findByUserEmailAndLocationName(email, name);
		if (manages.isEmpty()) {
			return ResponseEntity.badRequest().body("User is not a manager for this location");
		}
		
		managesRepository.deleteByUserEmailAndLocationName(email, name);
		return ResponseEntity.ok().build();
	}

	// List managers for a location (admin-only)
	@GetMapping("/{name}/managers")
	public ResponseEntity<?> listManagers(@AuthenticationPrincipal UserDetails principal, @PathVariable String name) {
		if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(managesRepository.findByLocationName(name));
	}

	// Get all users for manager assignment (admin-only)
	@GetMapping("/users")
	public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails principal) {
		if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
			return ResponseEntity.status(403).build();
		}
		// Return all users except admin
		List<com.example.demo.model.User> users = userRepository.findAll()
			.stream()
			.filter(user -> !user.getEmail().equals("admin@gmail.com"))
			.toList();
		return ResponseEntity.ok(users);
	}

	// Get users who are NOT managers of a specific location (admin-only)
	@GetMapping("/{name}/available-users")
	public ResponseEntity<?> getAvailableUsers(@AuthenticationPrincipal UserDetails principal, @PathVariable String name) {
		if (principal == null || !principal.getUsername().equals("admin@gmail.com")) {
			return ResponseEntity.status(403).build();
		}
		
		System.out.println("=== AVAILABLE USERS REQUEST ===");
		System.out.println("Location: " + name);
		
		// Get all users except admin
		List<com.example.demo.model.User> allUsers = userRepository.findAll()
			.stream()
			.filter(user -> !user.getEmail().equals("admin@gmail.com"))
			.toList();
		
		System.out.println("All users count: " + allUsers.size());
		allUsers.forEach(user -> System.out.println("  - " + user.getEmail() + " (" + user.getName() + ")"));
		
		// Get current managers of this location
		List<Manages> currentManagers = managesRepository.findByLocationName(name);
		List<String> managerEmails = currentManagers.stream()
			.map(m -> m.getUser().getEmail())
			.toList();
		
		System.out.println("Current managers count: " + managerEmails.size());
		managerEmails.forEach(email -> System.out.println("  - " + email));
		
		// Filter out current managers
		List<com.example.demo.model.User> availableUsers = allUsers.stream()
			.filter(user -> !managerEmails.contains(user.getEmail()))
			.toList();
		
		System.out.println("Available users count: " + availableUsers.size());
		availableUsers.forEach(user -> System.out.println("  - " + user.getEmail() + " (" + user.getName() + ")"));
		System.out.println("=== END AVAILABLE USERS REQUEST ===");
		
		return ResponseEntity.ok(availableUsers);
	}

	// Get locations managed by current user
	@GetMapping("/my-managed-locations")
	public ResponseEntity<?> getMyManagedLocations(@AuthenticationPrincipal UserDetails principal) {
		if (principal == null) {
			return ResponseEntity.status(401).build();
		}
		String email = principal.getUsername();
		List<Manages> manages = managesRepository.findByUserEmail(email);
		
		// Load location details with images for each managed location
		List<LocationService.LocationDetails> locationDetails = manages.stream()
			.map(manage -> locationService.getDetails(manage.getLocation().getName()))
			.toList();
		
		return ResponseEntity.ok(locationDetails);
	}
}


