package com.example.demo.search.dto;

public class SearchRequest {

	private String name;
	private String descriptionUi;
	private String descriptionPdf;

	private Integer reviewCountFrom;
	private Integer reviewCountTo;

	private Double avgPerformanceFrom;
	private Double avgPerformanceTo;
	private Double avgSoundLightFrom;
	private Double avgSoundLightTo;
	private Double avgVenueFrom;
	private Double avgVenueTo;
	private Double avgOverallFrom;
	private Double avgOverallTo;
	private Double avgTotalFrom;
	private Double avgTotalTo;

	// "AND" (default) ili "OR" izmedju polja name/descriptionUi/descriptionPdf
	private String operator;
	// "asc" (default) ili "desc" — sort po nazivu mesta
	private String sortOrder;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescriptionUi() { return descriptionUi; }
	public void setDescriptionUi(String descriptionUi) { this.descriptionUi = descriptionUi; }

	public String getDescriptionPdf() { return descriptionPdf; }
	public void setDescriptionPdf(String descriptionPdf) { this.descriptionPdf = descriptionPdf; }

	public Integer getReviewCountFrom() { return reviewCountFrom; }
	public void setReviewCountFrom(Integer reviewCountFrom) { this.reviewCountFrom = reviewCountFrom; }

	public Integer getReviewCountTo() { return reviewCountTo; }
	public void setReviewCountTo(Integer reviewCountTo) { this.reviewCountTo = reviewCountTo; }

	public Double getAvgPerformanceFrom() { return avgPerformanceFrom; }
	public void setAvgPerformanceFrom(Double avgPerformanceFrom) { this.avgPerformanceFrom = avgPerformanceFrom; }
	public Double getAvgPerformanceTo() { return avgPerformanceTo; }
	public void setAvgPerformanceTo(Double avgPerformanceTo) { this.avgPerformanceTo = avgPerformanceTo; }

	public Double getAvgSoundLightFrom() { return avgSoundLightFrom; }
	public void setAvgSoundLightFrom(Double avgSoundLightFrom) { this.avgSoundLightFrom = avgSoundLightFrom; }
	public Double getAvgSoundLightTo() { return avgSoundLightTo; }
	public void setAvgSoundLightTo(Double avgSoundLightTo) { this.avgSoundLightTo = avgSoundLightTo; }

	public Double getAvgVenueFrom() { return avgVenueFrom; }
	public void setAvgVenueFrom(Double avgVenueFrom) { this.avgVenueFrom = avgVenueFrom; }
	public Double getAvgVenueTo() { return avgVenueTo; }
	public void setAvgVenueTo(Double avgVenueTo) { this.avgVenueTo = avgVenueTo; }

	public Double getAvgOverallFrom() { return avgOverallFrom; }
	public void setAvgOverallFrom(Double avgOverallFrom) { this.avgOverallFrom = avgOverallFrom; }
	public Double getAvgOverallTo() { return avgOverallTo; }
	public void setAvgOverallTo(Double avgOverallTo) { this.avgOverallTo = avgOverallTo; }

	public Double getAvgTotalFrom() { return avgTotalFrom; }
	public void setAvgTotalFrom(Double avgTotalFrom) { this.avgTotalFrom = avgTotalFrom; }
	public Double getAvgTotalTo() { return avgTotalTo; }
	public void setAvgTotalTo(Double avgTotalTo) { this.avgTotalTo = avgTotalTo; }

	public String getOperator() { return operator; }
	public void setOperator(String operator) { this.operator = operator; }

	public String getSortOrder() { return sortOrder; }
	public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}
