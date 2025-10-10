package com.example.demo.controller;

import com.example.demo.model.AccountRequest;
import com.example.demo.model.RequestStatus;
import com.example.demo.repository.AccountRequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

record LoginRequest(String email, String password) {}
record MessageResponse(String message) {}
record TokenResponse(String token) {}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository, AccountRequestRepository accountRequestRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.accountRequestRepository = accountRequestRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Admin user does not have an AccountRequest, so we bypass this check for them.
        if (!"admin@gmail.com".equals(request.email())) {
            AccountRequest ar = accountRequestRepository.findById(request.email()).orElse(null);
            if (ar == null || ar.getStatus() != RequestStatus.ACCEPTED) {
                return ResponseEntity.badRequest().body(new MessageResponse("Registration not accepted or does not exist."));
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new TokenResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid credentials"));
        }
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout() {
		return ResponseEntity.ok(new MessageResponse("Logout successful"));
	}
}


