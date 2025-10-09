package com.example.demo.repository;

import com.example.demo.model.Manages;
import com.example.demo.model.Manages.ManagesId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagesRepository extends JpaRepository<Manages, ManagesId> {
	@Query("SELECT m FROM Manages m WHERE m.user.email = :userEmail AND m.location.name = :locationName")
	List<Manages> findByUserEmailAndLocationName(@Param("userEmail") String userEmail, @Param("locationName") String locationName);
	
	@Query("SELECT m FROM Manages m WHERE m.location.name = :locationName")
	List<Manages> findByLocationName(@Param("locationName") String locationName);
	
	@Query("SELECT m FROM Manages m WHERE m.user.email = :userEmail")
	List<Manages> findByUserEmail(@Param("userEmail") String userEmail);
	
	@Modifying
	@Query("DELETE FROM Manages m WHERE m.user.email = :userEmail AND m.location.name = :locationName")
	void deleteByUserEmailAndLocationName(@Param("userEmail") String userEmail, @Param("locationName") String locationName);
}


