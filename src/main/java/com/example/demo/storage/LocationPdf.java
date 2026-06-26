package com.example.demo.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class LocationPdf {

	@Id
	private String locationName;

	@Column(length = 1024, nullable = false)
	private String pdfKey;

	@Column(length = 512)
	private String originalFilename;

	public String getLocationName() { return locationName; }
	public void setLocationName(String locationName) { this.locationName = locationName; }

	public String getPdfKey() { return pdfKey; }
	public void setPdfKey(String pdfKey) { this.pdfKey = pdfKey; }

	public String getOriginalFilename() { return originalFilename; }
	public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
}
