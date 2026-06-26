package com.example.demo.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.demo.model.Location;
import com.example.demo.model.Rate;
import com.example.demo.model.Review;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.search.index.LocationDoc;
import com.example.demo.search.index.LocationIndexInit;
import com.example.demo.storage.LocationPdf;
import com.example.demo.storage.LocationPdfRepository;
import com.example.demo.storage.StorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationIndexer {

	private final ElasticsearchClient client;
	private final ReviewRepository reviewRepo;
	private final LocationPdfRepository pdfRepo;
	private final StorageService storage;
	private final PdfTextExtractor pdfExtractor;
	private final LocationRepository locationRepo;

	public LocationIndexer(ElasticsearchClient client, ReviewRepository reviewRepo,
			LocationPdfRepository pdfRepo, StorageService storage,
			PdfTextExtractor pdfExtractor, LocationRepository locationRepo) {
		this.client = client;
		this.reviewRepo = reviewRepo;
		this.pdfRepo = pdfRepo;
		this.storage = storage;
		this.pdfExtractor = pdfExtractor;
		this.locationRepo = locationRepo;
	}

	public void index(Location location) {
		try {
			LocationDoc doc = buildDoc(location);
			client.index(b -> b.index(LocationIndexInit.INDEX).id(location.getName()).document(doc));
		} catch (Exception e) {
			System.err.println("ES index failed for " + location.getName() + ": " + e.getMessage());
		}
	}

	public void reindex(Location location) {
		index(location);
	}

	public void delete(String locationName) {
		try {
			client.delete(b -> b.index(LocationIndexInit.INDEX).id(locationName));
		} catch (Exception e) {
			System.err.println("ES delete failed for " + locationName + ": " + e.getMessage());
		}
	}

	public int reindexAll() {
		int count = 0;
		for (Location loc : locationRepo.findAll()) {
			index(loc);
			count++;
		}
		return count;
	}

	private LocationDoc buildDoc(Location location) {
		LocationDoc doc = new LocationDoc();
		doc.setName(location.getName());
		doc.setDescriptionUi(location.getDescription());

		List<Review> reviews = reviewRepo.findByLocation(location);

		int reviewCount = 0;
		double sumP = 0, sumSL = 0, sumV = 0, sumO = 0;
		int cP = 0, cSL = 0, cV = 0, cO = 0;
		double sumPerReviewAvg = 0;
		int withRate = 0;

		for (Review r : reviews) {
			if (r.getDeleted() != null && r.getDeleted()) continue;
			// hidden review se racuna u prosek (po spec [M2])
			reviewCount++;

			Rate rate = r.getRate();
			if (rate == null) continue;

			List<Integer> values = new ArrayList<>();
			if (rate.getPerformance() != null) { sumP += rate.getPerformance(); cP++; values.add(rate.getPerformance()); }
			if (rate.getSoundAndLightning() != null) { sumSL += rate.getSoundAndLightning(); cSL++; values.add(rate.getSoundAndLightning()); }
			if (rate.getVenue() != null) { sumV += rate.getVenue(); cV++; values.add(rate.getVenue()); }
			if (rate.getOverallImpression() != null) { sumO += rate.getOverallImpression(); cO++; values.add(rate.getOverallImpression()); }

			if (!values.isEmpty()) {
				sumPerReviewAvg += values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
				withRate++;
			}
		}

		doc.setReviewCount(reviewCount);
		doc.setAvgPerformance(cP == 0 ? 0.0 : sumP / cP);
		doc.setAvgSoundLight(cSL == 0 ? 0.0 : sumSL / cSL);
		doc.setAvgVenue(cV == 0 ? 0.0 : sumV / cV);
		doc.setAvgOverall(cO == 0 ? 0.0 : sumO / cO);
		// avgTotal = isti pristup kao LocationService.computeAverageRating
		doc.setAvgTotal(withRate == 0 ? 0.0 : sumPerReviewAvg / withRate);

		// PDF -> Tika -> descriptionPdf
		LocationPdf pdf = pdfRepo.findById(location.getName()).orElse(null);
		if (pdf != null && pdf.getPdfKey() != null) {
			doc.setPdfKey(pdf.getPdfKey());
			String key = StorageService.stripUrlPrefix(pdf.getPdfKey());
			try (InputStream in = storage.download(key)) {
				doc.setDescriptionPdf(pdfExtractor.extract(in));
			} catch (Exception e) {
				System.err.println("PDF read failed for " + location.getName() + ": " + e.getMessage());
				doc.setDescriptionPdf("");
			}
		} else {
			doc.setDescriptionPdf("");
		}

		return doc;
	}
}