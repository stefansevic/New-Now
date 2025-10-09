package com.example.demo.repository;

import com.example.demo.model.Image;
import com.example.demo.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, String> {
    List<Image> findByLocation(Location location);
}


