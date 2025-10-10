package com.example.demo.service;

import com.example.demo.model.AccountRequest;
import com.example.demo.model.RequestStatus;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRequestRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountRequestService {

    private final AccountRequestRepository accountRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountRequestService(AccountRequestRepository accountRequestRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.accountRequestRepository = accountRequestRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

	public AccountRequest submit(AccountRequest request) {
		// disallow if user already exists
		if (userRepository.existsById(request.getEmail())) {
			throw new IllegalStateException("User already exists");
		}
		// disallow duplicate PENDING or ACCEPTED request for same email
		accountRequestRepository.findById(request.getEmail()).ifPresent(existing -> {
			if (existing.getStatus() == RequestStatus.PENDING || existing.getStatus() == RequestStatus.ACCEPTED) {
				throw new IllegalStateException("Request already exists and is pending or approved");
			}
		});
		request.setStatus(RequestStatus.PENDING);
		request.setCreatedAt(LocalDate.now());
		request.setRejectionReason(null);
		return accountRequestRepository.save(request);
	}

	public List<AccountRequest> listAll() {
		return accountRequestRepository.findAll();
	}

	public AccountRequest approve(String email) {
		AccountRequest req = accountRequestRepository.findById(email).orElseThrow();
		if (req.getStatus() != RequestStatus.PENDING) {
			throw new IllegalStateException("Only PENDING requests can be approved");
		}
		req.setStatus(RequestStatus.ACCEPTED);
		req.setRejectionReason(null);
		accountRequestRepository.save(req);
		
		// Create User with all data from AccountRequest
		User user = new User();
		user.setEmail(req.getEmail());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setName(req.getName());
		user.setPhoneNumber(req.getPhoneNumber());
		user.setBirthday(req.getBirthday());
		user.setAddress(req.getAddress());
		user.setCity(req.getCity());
		user.setCreatedAt(LocalDate.now());
		userRepository.save(user);
		return req;
	}

	public AccountRequest reject(String email, String reason) {
		AccountRequest req = accountRequestRepository.findById(email).orElseThrow();
		if (req.getStatus() != RequestStatus.PENDING) {
			throw new IllegalStateException("Only PENDING requests can be rejected");
		}
		req.setStatus(RequestStatus.REJECTED);
		req.setRejectionReason(reason);
		return accountRequestRepository.save(req);
	}
}


