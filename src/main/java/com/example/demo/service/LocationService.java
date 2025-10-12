package com.example.demo.service;

import com.example.demo.model.Image;
import com.example.demo.model.Location;
import com.example.demo.model.Review;
import com.example.demo.model.Rate;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;

    public LocationService(LocationRepository locationRepository, ReviewRepository reviewRepository, ImageRepository imageRepository) {
        this.locationRepository = locationRepository;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
    }

    // Kreiranje nove lokacije sa slikom
    public Location create(Location location, List<String> imagePaths) {
		if (location.getName() == null || location.getAddress() == null || location.getType() == null) {
			throw new IllegalArgumentException("Missing required fields");
		}
		if (imagePaths == null || imagePaths.isEmpty()) {
			throw new IllegalArgumentException("At least one image is required");
		}
		Location saved = locationRepository.save(location);
		for (String path : imagePaths) {
			Image img = new Image();
			img.setPath(path);
			img.setLocation(saved);
			imageRepository.save(img);
		}
		return saved;
	}

    public Location update(String name, String address, String type, String description) {
		Location loc = locationRepository.findById(name).orElseThrow();
		Optional.ofNullable(address).ifPresent(loc::setAddress);
		Optional.ofNullable(type).ifPresent(loc::setType);
		Optional.ofNullable(description).ifPresent(loc::setDescription);
		return locationRepository.save(loc);
	}

	// Racuna prosecnu ocenu lokacije na osnovu review-a (obrisani se ne racunaju)
	public double computeAverageRating(Location location) {
		List<Review> reviews = reviewRepository.findByLocation(location);
		if (reviews.isEmpty()) return 0.0;
		double sum = 0.0;
		int count = 0;
		for (Review r : reviews) {
			// Obrisani review-i se ne racunaju, a skriveni racunamo u prosek
			if (r.getDeleted() != null && r.getDeleted()) {
				continue;
			}
			
			Rate rate = r.getRate();
			if (rate == null) continue;
			List<Integer> values = new ArrayList<>();
			if (rate.getPerformance() != null) values.add(rate.getPerformance());
			if (rate.getSoundAndLightning() != null) values.add(rate.getSoundAndLightning());
			if (rate.getVenue() != null) values.add(rate.getVenue());
			if (rate.getOverallImpression() != null) values.add(rate.getOverallImpression());
			if (!values.isEmpty()) {
				double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
				sum += avg;
				count++;
			}
		}
		return count == 0 ? 0.0 : sum / count;
	}

    public LocationDetails getDetails(String name) {
        Location location = locationRepository.findById(name).orElseThrow();
        double avgRating = computeAverageRating(location);
        List<Image> images = imageRepository.findByLocation(location);
        return new LocationDetails(location, avgRating, images);
    }

	public List<LocationDetails> listAll() {
		List<Location> locations = locationRepository.findAll();
		List<LocationDetails> details = new ArrayList<>();
		for (Location l : locations) {
			List<Image> images = imageRepository.findByLocation(l);
			details.add(new LocationDetails(l, computeAverageRating(l), images));
		}
		return details;
	}

    public void delete(String name) {
        Location location = locationRepository.findById(name).orElseThrow();
        
        // Delete associated images from database
        List<Image> images = imageRepository.findByLocation(location);
        imageRepository.deleteAll(images);
        
        // Delete image files from disk
        for (Image image : images) {
            try {
                String imagePath = image.getPath();
                if (imagePath.startsWith("/uploads/")) {
                    String fileName = imagePath.substring("/uploads/".length());
                    java.nio.file.Path filePath = java.nio.file.Paths.get("uploads", fileName);
                    java.nio.file.Files.deleteIfExists(filePath);
                }
            } catch (Exception e) {
                System.err.println("Failed to delete image file: " + image.getPath());
            }
        }
        
        // Delete the location
        locationRepository.deleteById(name);
    }

    public record LocationDetails(Location location, double averageRating, List<Image> images) {}
}


