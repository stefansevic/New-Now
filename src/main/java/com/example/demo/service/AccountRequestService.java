package com.example.demo.service;

import com.example.demo.model.AccountRequest;
import com.example.demo.model.RequestStatus;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRequestRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
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
		request.setStatus(RequestStatus.PENDING);
		return accountRequestRepository.save(request);
	}

	public List<AccountRequest> listAll() {
		return accountRequestRepository.findAll();
	}

    public AccountRequest approve(String email, String name) {
		AccountRequest req = accountRequestRepository.findById(email).orElseThrow();
		req.setStatus(RequestStatus.ACCEPTED);
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
		req.setStatus(RequestStatus.REJECTED);
		req.setRejectionReason(reason);
		return accountRequestRepository.save(req);
	}
}


