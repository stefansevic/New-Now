package com.example.demo.repository;

import com.example.demo.model.Location;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, LocalDateTime> {
	List<Review> findByLocation(Location location);
	List<Review> findByUser(User user);
}


