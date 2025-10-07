package com.example.demo.repository;

import com.example.demo.model.Manages;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagesRepository extends JpaRepository<Manages, LocalDate> {
	List<Manages> findByUserEmailAndLocationName(String userEmail, String locationName);
	List<Manages> findByLocationName(String locationName);
	void deleteByUserEmailAndLocationName(String userEmail, String locationName);
}


