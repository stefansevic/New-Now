package com.example.demo.search.index;

public class LocationDoc {

	private String name;
	private String descriptionUi;
	private String descriptionPdf;
	private Integer reviewCount;
	private Double avgPerformance;
	private Double avgSoundLight;
	private Double avgVenue;
	private Double avgOverall;
	private Double avgTotal;
	private String pdfKey;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescriptionUi() { return descriptionUi; }
	public void setDescriptionUi(String descriptionUi) { this.descriptionUi = descriptionUi; }

	public String getDescriptionPdf() { return descriptionPdf; }
	public void setDescriptionPdf(String descriptionPdf) { this.descriptionPdf = descriptionPdf; }

	public Integer getReviewCount() { return reviewCount; }
	public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

	public Double getAvgPerformance() { return avgPerformance; }
	public void setAvgPerformance(Double avgPerformance) { this.avgPerformance = avgPerformance; }

	public Double getAvgSoundLight() { return avgSoundLight; }
	public void setAvgSoundLight(Double avgSoundLight) { this.avgSoundLight = avgSoundLight; }

	public Double getAvgVenue() { return avgVenue; }
	public void setAvgVenue(Double avgVenue) { this.avgVenue = avgVenue; }

	public Double getAvgOverall() { return avgOverall; }
	public void setAvgOverall(Double avgOverall) { this.avgOverall = avgOverall; }

	public Double getAvgTotal() { return avgTotal; }
	public void setAvgTotal(Double avgTotal) { this.avgTotal = avgTotal; }

	public String getPdfKey() { return pdfKey; }
	public void setPdfKey(String pdfKey) { this.pdfKey = pdfKey; }
}