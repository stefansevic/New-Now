package com.example.demo.repository;

import com.example.demo.model.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateRepository extends JpaRepository<Rate, Integer> {
}

