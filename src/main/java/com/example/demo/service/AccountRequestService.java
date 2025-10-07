package com.example.demo.service;

import com.example.demo.model.AccountRequest;
import com.example.demo.model.RequestStatus;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRequestRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class AccountRequestService {

    private final AccountRequestRepository accountRequestRepository;
    private final UserRepository userRepository;

    public AccountRequestService(AccountRequestRepository accountRequestRepository, UserRepository userRepository) {
        this.accountRequestRepository = accountRequestRepository;
        this.userRepository = userRepository;
    }

	public AccountRequest submit(AccountRequest request) {
		// disallow if user already exists
		if (userRepository.existsById(request.getEmail())) {
			throw new IllegalStateException("User already exists");
		}
		// disallow duplicate non-rejected request for same email
		accountRequestRepository.findById(request.getEmail()).ifPresent(existing -> {
			if (existing.getStatus() != RequestStatus.REJECTED) {
				throw new IllegalStateException("Request already exists");
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

	public AccountRequest approve(String email, String name) {
		AccountRequest req = accountRequestRepository.findById(email).orElseThrow();
		if (req.getStatus() != RequestStatus.PENDING) {
			throw new IllegalStateException("Only PENDING requests can be approved");
		}
		req.setStatus(RequestStatus.ACCEPTED);
		req.setRejectionReason(null);
		accountRequestRepository.save(req);
		User user = new User();
		user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
		user.setName(name);
		user.setAddress(req.getAddress());
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


