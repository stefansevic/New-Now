package com.example.demo.controller;

import com.example.demo.model.AccountRequest;
import com.example.demo.service.AccountRequestService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account-requests")
public class AccountRequestController {

	private final AccountRequestService service;

	public AccountRequestController(AccountRequestService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<AccountRequest> submit(@RequestBody AccountRequest request) {
		return ResponseEntity.ok(service.submit(request));
	}

	@GetMapping
	public ResponseEntity<List<AccountRequest>> list() {
		return ResponseEntity.ok(service.listAll());
	}

	@PostMapping("/{email}/approve")
	public ResponseEntity<AccountRequest> approve(@PathVariable String email, @RequestParam String name) {
		return ResponseEntity.ok(service.approve(email, name));
	}

	@PostMapping("/{email}/reject")
	public ResponseEntity<AccountRequest> reject(@PathVariable String email, @RequestParam String reason) {
		return ResponseEntity.ok(service.reject(email, reason));
	}
}


