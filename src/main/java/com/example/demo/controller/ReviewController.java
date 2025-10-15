package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

record CreateReviewRequest(
    String locationName,
    String eventName,
    String commentText,
    Integer performance,
    Integer soundAndLightning,
    Integer venue,
    Integer overallImpression
) {}

record CreateCommentRequest(String text, String parentCommentCreatedAt) {}

record ReviewWithComments(Review review, List<Comment> comments) {}

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RateRepository rateRepository;
    private final CommentRepository commentRepository;
    private final ManagesRepository managesRepository;

    public ReviewController(ReviewRepository reviewRepository, LocationRepository locationRepository,
                          EventRepository eventRepository, UserRepository userRepository,
                          RateRepository rateRepository, CommentRepository commentRepository,
                          ManagesRepository managesRepository) {
        this.reviewRepository = reviewRepository;
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.rateRepository = rateRepository;
        this.commentRepository = commentRepository;
        this.managesRepository = managesRepository;
    }

    // Kreiranje review-a za lokaciju (samo user koji nije manager za tu lokaciju ili admin)
    @PostMapping
    @Transactional
    public ResponseEntity<?> createReview(@AuthenticationPrincipal UserDetails principal, 
                                         @RequestBody CreateReviewRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getUsername();
        
        // Admin ne moze da ostavlja review-e
        if (email.equals("admin@gmail.com")) {
            return ResponseEntity.status(403).body("Administrators cannot leave reviews");
        }

        // Get location
        Location location = locationRepository.findById(req.locationName()).orElse(null);
        if (location == null) return ResponseEntity.badRequest().body("Location not found");
        
        // Menadzer lokacije ne moze da ostavlja review za tu lokaciju
        boolean isManager = !managesRepository.findByUserEmailAndLocationName(email, req.locationName()).isEmpty();
        if (isManager) {
            return ResponseEntity.status(403).body("Managers cannot leave reviews on locations they manage");
        }

        // Get event
        Event event = eventRepository.findById(req.eventName()).orElse(null);
        if (event == null) return ResponseEntity.badRequest().body("Event not found");

        // event mora biti recurrent i mora biti u proslosti
        if (event.getRecurrent() == null || !event.getRecurrent()) {
            return ResponseEntity.badRequest().body("Reviews can only be left for recurrent events");
        }

        if (event.getDate() == null || !event.getDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("Reviews can only be left for past events");
        }

        // event mora pripadati lokaciji
        if (!event.getLocation().getName().equals(location.getName())) {
            return ResponseEntity.badRequest().body("Event does not belong to this location");
        }

        // koliko puta se reccurent dogadjaj dogodio
        int eventCount = eventRepository.countByNameAndLocationAndDateBefore(
            event.getName(), 
            location, 
            LocalDate.now()
        );

        // Get user
        User user = userRepository.findById(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // validacija (ocenjivanje 1-10)
        if (req.performance() != null && (req.performance() < 1 || req.performance() > 10)) {
            return ResponseEntity.badRequest().body("Performance rating must be between 1 and 10");
        }
        if (req.soundAndLightning() != null && (req.soundAndLightning() < 1 || req.soundAndLightning() > 10)) {
            return ResponseEntity.badRequest().body("Sound & Lightning rating must be between 1 and 10");
        }
        if (req.venue() != null && (req.venue() < 1 || req.venue() > 10)) {
            return ResponseEntity.badRequest().body("Venue rating must be between 1 and 10");
        }
        if (req.overallImpression() != null && (req.overallImpression() < 1 || req.overallImpression() > 10)) {
            return ResponseEntity.badRequest().body("Overall impression rating must be between 1 and 10");
        }

        // Napravi ocenu ako se barem jedan entitet ocenio
        Rate rate = null;
        if (req.performance() != null || req.soundAndLightning() != null || 
            req.venue() != null || req.overallImpression() != null) {
            
            rate = new Rate();
            rate.setPerformance(req.performance());
            rate.setSoundAndLightning(req.soundAndLightning());
            rate.setVenue(req.venue());
            rate.setOverallImpression(req.overallImpression());
            rate = rateRepository.save(rate);
        }

        // Napravi review
        Review review = new Review();
        review.setCreatedAt(LocalDateTime.now());
        review.setUser(user);
        review.setLocation(location);
        review.setEvent(event);
        review.setEventCount(eventCount);
        review.setHidden(false);
        review.setDeleted(false);
        review.setRate(rate);

        review = reviewRepository.save(review);

        // ako user ukuca text u textfield, napravi i komentar
        if (req.commentText() != null && !req.commentText().trim().isEmpty()) {
            Comment comment = new Comment();
            comment.setCreatedAt(LocalDateTime.now());
            comment.setText(req.commentText());
            comment.setUser(user);
            comment.setReview(review);
            comment.setParent(null);
            commentRepository.save(comment);
        }

        return ResponseEntity.ok(review);
    }

    @GetMapping("/location/{locationName}")
    public ResponseEntity<?> getReviewsByLocation(@PathVariable String locationName) {
        Location location = locationRepository.findById(locationName).orElse(null);
        if (location == null) return ResponseEntity.badRequest().build();

        List<Review> reviews = reviewRepository.findByLocation(location);
        
        // Get comments za svaki review
        var reviewsWithComments = reviews.stream()
            .map(review -> {
                List<Comment> comments = commentRepository.findByReview(review);
                return new ReviewWithComments(review, comments);
            })
            .toList();
        
        return ResponseEntity.ok(reviewsWithComments);
    }

    // Get evenet koji se moze oceniti (recurrent i u proslosti)
    @GetMapping("/location/{locationName}/eligible-events")
    public ResponseEntity<List<Event>> getEligibleEvents(@PathVariable String locationName) {
        Location location = locationRepository.findById(locationName).orElse(null);
        if (location == null) return ResponseEntity.badRequest().build();

        // Get all events za tu lokaciju
        List<Event> allEvents = eventRepository.findAll().stream()
            .filter(e -> e.getLocation() != null && e.getLocation().getName().equals(locationName))
            .filter(e -> e.getRecurrent() != null && e.getRecurrent()) // mora biti recurrent
            .filter(e -> e.getDate() != null && e.getDate().isBefore(LocalDate.now())) // mora biti u proslosti
            .toList();

        System.out.println("=== ELIGIBLE EVENTS DEBUG ===");
        System.out.println("Location: " + locationName);
        System.out.println("Total events in DB: " + eventRepository.count());
        System.out.println("Events for this location: " + eventRepository.findAll().stream()
            .filter(e -> e.getLocation() != null && e.getLocation().getName().equals(locationName))
            .count());
        System.out.println("Recurrent events: " + eventRepository.findAll().stream()
            .filter(e -> e.getLocation() != null && e.getLocation().getName().equals(locationName))
            .filter(e -> e.getRecurrent() != null && e.getRecurrent())
            .count());
        System.out.println("Past recurrent events: " + allEvents.size());
        allEvents.forEach(e -> System.out.println("  - " + e.getName() + " (date: " + e.getDate() + ", recurrent: " + e.getRecurrent() + ")"));
        System.out.println("=== END DEBUG ===");

        return ResponseEntity.ok(allEvents);
    }

    // Sakrivanje review-a
    @PutMapping("/{createdAt}/hide")
    public ResponseEntity<?> hideReview(@AuthenticationPrincipal UserDetails principal, 
                                       @PathVariable String createdAt) {
        if (principal == null) return ResponseEntity.status(401).build();

        LocalDateTime reviewTime = LocalDateTime.parse(createdAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        // Samo menadzer lokacije moze da sakrije review
        String email = principal.getUsername();
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can hide reviews");

        review.setHidden(true);
        reviewRepository.save(review);
        return ResponseEntity.ok().build();
    }

    // Unhide review
    @PutMapping("/{createdAt}/unhide")
    public ResponseEntity<?> unhideReview(@AuthenticationPrincipal UserDetails principal, 
                                         @PathVariable String createdAt) {
        if (principal == null) return ResponseEntity.status(401).build();

        LocalDateTime reviewTime = LocalDateTime.parse(createdAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        // proveri jel user manager te lokacije
        String email = principal.getUsername();
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can unhide reviews");

        review.setHidden(false);
        reviewRepository.save(review);
        return ResponseEntity.ok().build();
    }

    // Delete review 
    @DeleteMapping("/{createdAt}")
    public ResponseEntity<?> deleteReview(@AuthenticationPrincipal UserDetails principal, 
                                         @PathVariable String createdAt) {
        if (principal == null) return ResponseEntity.status(401).build();

        LocalDateTime reviewTime = LocalDateTime.parse(createdAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        // proveri jel user manager te lokacije
        String email = principal.getUsername();
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can delete reviews");

        review.setDeleted(true);
        reviewRepository.save(review);
        return ResponseEntity.ok().build();
    }

    // Add comment to review (reply)
    @PostMapping("/{reviewCreatedAt}/comments")
    @Transactional
    public ResponseEntity<?> addComment(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable String reviewCreatedAt,
                                       @RequestBody CreateCommentRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        LocalDateTime reviewTime = LocalDateTime.parse(reviewCreatedAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        String email = principal.getUsername();
        User user = userRepository.findById(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // proveri jel user manager te lokacije
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();

        // nadji parent comment
        Comment parentComment = null;
        if (req.parentCommentCreatedAt() != null && !req.parentCommentCreatedAt().isEmpty()) {
            LocalDateTime parentTime = LocalDateTime.parse(req.parentCommentCreatedAt());
            parentComment = commentRepository.findById(parentTime).orElse(null);
            if (parentComment == null) {
                return ResponseEntity.badRequest().body("Parent comment not found");
            }

            // Manager moze da replayuye na sve komentare
            // User moze da replayuye samo na managerov komentar 
            if (!isManager) {
                
                boolean parentIsFromManager = !managesRepository
                    .findByUserEmailAndLocationName(
                        parentComment.getUser().getEmail(), 
                        review.getLocation().getName()
                    ).isEmpty();
                
                if (!parentIsFromManager) {
                    return ResponseEntity.status(403)
                        .body("Regular users can only reply to manager comments");
                }
            }
        }

        // Create comment
        Comment comment = new Comment();
        comment.setCreatedAt(LocalDateTime.now());
        comment.setText(req.text());
        comment.setUser(user);
        comment.setReview(review);
        comment.setParent(parentComment);
        
        commentRepository.save(comment);
        return ResponseEntity.ok(comment);
    }
}

