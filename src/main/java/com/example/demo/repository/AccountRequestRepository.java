package com.example.demo.repository;

import com.example.demo.model.AccountRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRequestRepository extends JpaRepository<AccountRequest, String> {
}


