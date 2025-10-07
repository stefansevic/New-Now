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

	public Location create(Location location, List<String> imagePaths) {
		if (location.getName() == null || location.getAddress() == null || location.getType() == null || location.getDescription() == null) {
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

	public double computeAverageRating(Location location) {
		List<Review> reviews = reviewRepository.findByLocation(location);
		if (reviews.isEmpty()) return 0.0;
		double sum = 0.0;
		int count = 0;
		for (Review r : reviews) {
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
        return new LocationDetails(location, avgRating);
    }

	public List<Location> listAll() {
		return locationRepository.findAll();
	}

    public record LocationDetails(Location location, double averageRating) {}
}


