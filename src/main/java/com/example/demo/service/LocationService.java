package com.example.demo.service;

import com.example.demo.model.Image;
import com.example.demo.model.Location;
import com.example.demo.model.Review;
import com.example.demo.model.Rate;
import com.example.demo.repository.ImageRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.storage.LocationPdf;
import com.example.demo.storage.LocationPdfRepository;
import com.example.demo.storage.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final LocationPdfRepository pdfRepository;
    private final StorageService storage;

    public LocationService(LocationRepository locationRepository, ReviewRepository reviewRepository,
                           ImageRepository imageRepository, LocationPdfRepository pdfRepository,
                           StorageService storage) {
        this.locationRepository = locationRepository;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.pdfRepository = pdfRepository;
        this.storage = storage;
    }

    // Kreiranje nove lokacije sa slikama i opcionim PDF-om
    public Location create(Location location, List<String> imagePaths, String pdfKey) {
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
		if (pdfKey != null && !pdfKey.isBlank()) {
			LocationPdf p = new LocationPdf();
			p.setLocationName(saved.getName());
			p.setPdfKey(pdfKey);
			pdfRepository.save(p);
		}
		return saved;
	}

    public Location update(String name, String address, String type, String description, String pdfKey) {
		Location loc = locationRepository.findById(name).orElseThrow();
		Optional.ofNullable(address).ifPresent(loc::setAddress);
		Optional.ofNullable(type).ifPresent(loc::setType);
		Optional.ofNullable(description).ifPresent(loc::setDescription);
		Location saved = locationRepository.save(loc);

		// pdfKey == null znaci ne diraj; prazan string znaci ukloni
		if (pdfKey != null) {
			Optional<LocationPdf> existing = pdfRepository.findById(name);
			if (pdfKey.isBlank()) {
				existing.ifPresent(p -> {
					storage.delete(StorageService.stripUrlPrefix(p.getPdfKey()));
					pdfRepository.delete(p);
				});
			} else if (existing.isPresent()) {
				LocationPdf p = existing.get();
				if (p.getPdfKey() != null && !p.getPdfKey().equals(pdfKey)) {
					storage.delete(StorageService.stripUrlPrefix(p.getPdfKey()));
				}
				p.setPdfKey(pdfKey);
				pdfRepository.save(p);
			} else {
				LocationPdf p = new LocationPdf();
				p.setLocationName(saved.getName());
				p.setPdfKey(pdfKey);
				pdfRepository.save(p);
			}
		}
		return saved;
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
        String pdfKey = pdfRepository.findById(name).map(LocationPdf::getPdfKey).orElse(null);
        return new LocationDetails(location, avgRating, images, pdfKey);
    }

	public List<LocationDetails> listAll() {
		List<Location> locations = locationRepository.findAll();
		List<LocationDetails> details = new ArrayList<>();
		for (Location l : locations) {
			List<Image> images = imageRepository.findByLocation(l);
			String pdfKey = pdfRepository.findById(l.getName()).map(LocationPdf::getPdfKey).orElse(null);
			details.add(new LocationDetails(l, computeAverageRating(l), images, pdfKey));
		}
		return details;
	}

    public void delete(String name) {
        Location location = locationRepository.findById(name).orElseThrow();

        List<Image> images = imageRepository.findByLocation(location);
        for (Image image : images) {
            storage.delete(StorageService.stripUrlPrefix(image.getPath()));
        }
        imageRepository.deleteAll(images);

        pdfRepository.findById(name).ifPresent(p -> {
            storage.delete(StorageService.stripUrlPrefix(p.getPdfKey()));
            pdfRepository.delete(p);
        });

        locationRepository.deleteById(name);
    }

    public record LocationDetails(Location location, double averageRating, List<Image> images, String pdfKey) {}
}
