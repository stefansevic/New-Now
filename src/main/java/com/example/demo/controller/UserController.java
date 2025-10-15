package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Review;
import com.example.demo.model.Manages;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.ManagesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {}

record UpdateProfileRequest(
    String name,
    String phoneNumber,
    String birthday,
    String address,
    String city
) {}

record ImageUploadResponse(String imagePath, String message) {}

record UserProfileResponse(
    String email,
    String name,
    String phoneNumber,
    String birthday,
    String address,
    String city,
    String imagePath,
    LocalDate createdAt,
    List<Review> reviews,
    List<Manages> managedLocations
) {}

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ManagesRepository managesRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
        UserRepository userRepository,
        ReviewRepository reviewRepository,
        ManagesRepository managesRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.managesRepository = managesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Preuzimanje profila trenutnog korisnika
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principal.getUsername();
        User user = userRepository.findById(email).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Ucitavanje review-a korisnika
        List<Review> reviews = reviewRepository.findByUser(user);

        // Ucitavanje lokacija koje korisnik menadzira
        List<Manages> managedLocations = managesRepository.findByUserEmail(email);

        UserProfileResponse response = new UserProfileResponse(
            user.getEmail(),
            user.getName(),
            user.getPhoneNumber(),
            user.getBirthday() != null ? user.getBirthday().toString() : null,
            user.getAddress(),
            user.getCity(),
            user.getImagePath(),
            user.getCreatedAt(),
            reviews,
            managedLocations
        );

        return ResponseEntity.ok(response);
    }

    // Promena lozinke korisnika
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
        @AuthenticationPrincipal UserDetails principal,
        @RequestBody ChangePasswordRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principal.getUsername();
        User user = userRepository.findById(email).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Provera trenutne lozinke
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        // Provera da li se nove lozinke poklapaju
        if (!request.newPassword().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body("New passwords do not match");
        }

        // Provera minimalne duzine lozinke
        if (request.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body("New password must be at least 6 characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    // Azuriranje profila korisnika
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
        @AuthenticationPrincipal UserDetails principal,
        @RequestBody UpdateProfileRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principal.getUsername();
        User user = userRepository.findById(email).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Azuriranje polja profila
        if (request.name() != null) user.setName(request.name());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.birthday() != null && !request.birthday().isEmpty()) {
            user.setBirthday(LocalDate.parse(request.birthday()));
        }
        if (request.address() != null) user.setAddress(request.address());
        if (request.city() != null) user.setCity(request.city());

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    // Upload profile image
    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadProfileImage(
        @AuthenticationPrincipal UserDetails principal,
        @RequestParam("file") MultipartFile file
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String email = principal.getUsername();
        User user = userRepository.findById(email).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Kreiranje uploads direktorijuma ako ne postoji
            Path uploadDir = Paths.get("uploads/profiles");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generisanje jedinstvenog naziva fajla (da kasnije ne dodje do preklapanja)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update user image path
            String imagePath = "/uploads/profiles/" + filename;
            user.setImagePath(imagePath);
            userRepository.save(user);

            System.out.println("Image uploaded successfully: " + imagePath);
            return ResponseEntity.ok().body(new ImageUploadResponse(imagePath, "Image uploaded successfully"));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload image: " + e.getMessage());
        }
    }
}

