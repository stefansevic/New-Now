package com.example.demo.controller;

import com.example.demo.model.AccountRequest;
import com.example.demo.model.RequestStatus;
import com.example.demo.repository.AccountRequestRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

record LoginRequest(String email, String password) {}
record MessageResponse(String message) {}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final AccountRequestRepository accountRequestRepository;

	public AuthController(UserRepository userRepository, AccountRequestRepository accountRequestRepository) {
		this.userRepository = userRepository;
		this.accountRequestRepository = accountRequestRepository;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		// Check request status
		AccountRequest ar = accountRequestRepository.findById(request.email()).orElse(null);
		if (ar == null || ar.getStatus() != RequestStatus.ACCEPTED) {
			return ResponseEntity.badRequest().body(new MessageResponse("Registration not accepted"));
		}
		return userRepository.findById(request.email())
				.filter(u -> u.getPassword().equals(request.password()))
				.<ResponseEntity<?>>map(u -> ResponseEntity.ok(new MessageResponse("Login successful")))
				.orElseGet(() -> ResponseEntity.badRequest().body(new MessageResponse("Invalid credentials")));
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout() {
		return ResponseEntity.ok(new MessageResponse("Logout successful"));
	}
}


