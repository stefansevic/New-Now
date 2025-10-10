package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.Location;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, String> {
	List<Event> findByLocationAndDateAfter(Location location, LocalDate date);
	List<Event> findByNameAndLocationAndDateBefore(String name, Location location, LocalDate date);
	int countByNameAndLocationAndDateBefore(String name, Location location, LocalDate date);
}


