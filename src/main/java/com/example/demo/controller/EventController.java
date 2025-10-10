package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.model.Location;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.ManagesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

record CreateEventRequest(String name, String address, String type, LocalDate date, Double price, Boolean recurrent, String locationName, String imagePath) {}
record UpdateEventRequest(String address, String type, LocalDate date, Double price, Boolean recurrent, String imagePath) {}

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final ManagesRepository managesRepository;

    public EventController(EventRepository eventRepository, LocationRepository locationRepository, ManagesRepository managesRepository) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.managesRepository = managesRepository;
    }

    // Create event - only managers of the location
    @PostMapping
    public ResponseEntity<?> createEvent(@AuthenticationPrincipal UserDetails principal, @RequestBody CreateEventRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        String email = principal.getUsername();
        boolean isManager = !managesRepository.findByUserEmailAndLocationName(email, req.locationName()).isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can create events");

        Location location = locationRepository.findById(req.locationName()).orElse(null);
        if (location == null) return ResponseEntity.badRequest().body("Location not found");

        Event event = new Event();
        event.setName(req.name());
        event.setAddress(req.address());
        event.setType(req.type());
        event.setDate(req.date());
        event.setPrice(req.price());
        event.setRecurrent(req.recurrent());
        event.setImagePath(req.imagePath());
        event.setLocation(location);

        return ResponseEntity.ok(eventRepository.save(event));
    }

    // Update event - only managers of the location
    @PutMapping("/{name}")
    public ResponseEntity<?> updateEvent(@AuthenticationPrincipal UserDetails principal, @PathVariable String name, @RequestBody UpdateEventRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();

        Event event = eventRepository.findById(name).orElse(null);
        if (event == null) return ResponseEntity.badRequest().body("Event not found");

        String email = principal.getUsername();
        boolean isManager = !managesRepository.findByUserEmailAndLocationName(email, event.getLocation().getName()).isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can update events");

        event.setAddress(req.address());
        event.setType(req.type());
        event.setDate(req.date());
        event.setPrice(req.price());
        event.setRecurrent(req.recurrent());
        event.setImagePath(req.imagePath());

        return ResponseEntity.ok(eventRepository.save(event));
    }

    // Delete event - only managers of the location
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteEvent(@AuthenticationPrincipal UserDetails principal, @PathVariable String name) {
        if (principal == null) return ResponseEntity.status(401).build();

        Event event = eventRepository.findById(name).orElse(null);
        if (event == null) return ResponseEntity.badRequest().body("Event not found");

        String email = principal.getUsername();
        boolean isManager = !managesRepository.findByUserEmailAndLocationName(email, event.getLocation().getName()).isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can delete events");

        eventRepository.delete(event);
        return ResponseEntity.ok().build();
    }

    // Get events for a location (public)
    @GetMapping("/location/{locationName}")
    public ResponseEntity<List<Event>> getEventsByLocation(@PathVariable String locationName) {
        Location location = locationRepository.findById(locationName).orElse(null);
        if (location == null) return ResponseEntity.badRequest().build();

        List<Event> events = eventRepository.findByLocationAndDateAfter(location, LocalDate.now().minusDays(1));
        return ResponseEntity.ok(events);
    }

    // Get single event (public)
    @GetMapping("/{name}")
    public ResponseEntity<Event> getEvent(@PathVariable String name) {
        Event event = eventRepository.findById(name).orElse(null);
        if (event == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(event);
    }
}

