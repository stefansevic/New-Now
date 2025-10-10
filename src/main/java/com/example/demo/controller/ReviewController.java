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

    @PostMapping
    @Transactional
    public ResponseEntity<?> createReview(@AuthenticationPrincipal UserDetails principal, 
                                         @RequestBody CreateReviewRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getUsername();
        
        // Check if user is admin
        if (email.equals("admin@gmail.com")) {
            return ResponseEntity.status(403).body("Administrators cannot leave reviews");
        }

        // Get location
        Location location = locationRepository.findById(req.locationName()).orElse(null);
        if (location == null) return ResponseEntity.badRequest().body("Location not found");
        
        // Check if user is manager of this location
        boolean isManager = !managesRepository.findByUserEmailAndLocationName(email, req.locationName()).isEmpty();
        if (isManager) {
            return ResponseEntity.status(403).body("Managers cannot leave reviews on locations they manage");
        }

        // Get event
        Event event = eventRepository.findById(req.eventName()).orElse(null);
        if (event == null) return ResponseEntity.badRequest().body("Event not found");

        // Validation 1: Event must be recurrent
        if (event.getRecurrent() == null || !event.getRecurrent()) {
            return ResponseEntity.badRequest().body("Reviews can only be left for recurrent events");
        }

        // Validation 2: Event must have already occurred
        if (event.getDate() == null || !event.getDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("Reviews can only be left for past events");
        }

        // Validation 3: Event must belong to the location
        if (!event.getLocation().getName().equals(location.getName())) {
            return ResponseEntity.badRequest().body("Event does not belong to this location");
        }

        // Calculate event count (how many times this recurrent event has occurred)
        int eventCount = eventRepository.countByNameAndLocationAndDateBefore(
            event.getName(), 
            location, 
            LocalDate.now()
        );

        // Get user
        User user = userRepository.findById(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // Validate ratings are between 1-10
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

        // Create Rate if at least one rating is provided
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

        // Create Review
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

        // Create Comment if text is provided
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
        
        // Get comments for each review
        var reviewsWithComments = reviews.stream()
            .map(review -> {
                List<Comment> comments = commentRepository.findByReview(review);
                return new ReviewWithComments(review, comments);
            })
            .toList();
        
        return ResponseEntity.ok(reviewsWithComments);
    }

    // Get eligible events for review (recurrent and past events)
    @GetMapping("/location/{locationName}/eligible-events")
    public ResponseEntity<List<Event>> getEligibleEvents(@PathVariable String locationName) {
        Location location = locationRepository.findById(locationName).orElse(null);
        if (location == null) return ResponseEntity.badRequest().build();

        // Get all events for this location
        List<Event> allEvents = eventRepository.findAll().stream()
            .filter(e -> e.getLocation() != null && e.getLocation().getName().equals(locationName))
            .filter(e -> e.getRecurrent() != null && e.getRecurrent()) // Must be recurrent
            .filter(e -> e.getDate() != null && e.getDate().isBefore(LocalDate.now())) // Must be in the past
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

    // Hide review (manager only)
    @PutMapping("/{createdAt}/hide")
    public ResponseEntity<?> hideReview(@AuthenticationPrincipal UserDetails principal, 
                                       @PathVariable String createdAt) {
        if (principal == null) return ResponseEntity.status(401).build();

        LocalDateTime reviewTime = LocalDateTime.parse(createdAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        // Check if user is manager of this location
        String email = principal.getUsername();
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can hide reviews");

        review.setHidden(true);
        reviewRepository.save(review);
        return ResponseEntity.ok().build();
    }

    // Unhide review (manager only)
    @PutMapping("/{createdAt}/unhide")
    public ResponseEntity<?> unhideReview(@AuthenticationPrincipal UserDetails principal, 
                                         @PathVariable String createdAt) {
        if (principal == null) return ResponseEntity.status(401).build();

        LocalDateTime reviewTime = LocalDateTime.parse(createdAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        // Check if user is manager of this location
        String email = principal.getUsername();
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();
        
        if (!isManager) return ResponseEntity.status(403).body("Only managers can unhide reviews");

        review.setHidden(false);
        reviewRepository.save(review);
        return ResponseEntity.ok().build();
    }

    // Delete review (manager only - logical delete)
    @DeleteMapping("/{createdAt}")
    public ResponseEntity<?> deleteReview(@AuthenticationPrincipal UserDetails principal, 
                                         @PathVariable String createdAt) {
        if (principal == null) return ResponseEntity.status(401).build();

        LocalDateTime reviewTime = LocalDateTime.parse(createdAt);
        Review review = reviewRepository.findById(reviewTime).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body("Review not found");

        // Check if user is manager of this location
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

        // Check if user is manager of this location
        boolean isManager = !managesRepository
            .findByUserEmailAndLocationName(email, review.getLocation().getName())
            .isEmpty();

        // Find parent comment if specified
        Comment parentComment = null;
        if (req.parentCommentCreatedAt() != null && !req.parentCommentCreatedAt().isEmpty()) {
            LocalDateTime parentTime = LocalDateTime.parse(req.parentCommentCreatedAt());
            parentComment = commentRepository.findById(parentTime).orElse(null);
            if (parentComment == null) {
                return ResponseEntity.badRequest().body("Parent comment not found");
            }

            // Validation: If parent exists, check reply permissions
            // Manager can always reply to user comments
            // User can only reply to manager's comments (parent.user must be manager)
            if (!isManager) {
                // Regular user trying to reply - parent must be from a manager
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

