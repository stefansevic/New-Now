package com.example.demo.search.dto;

import java.util.List;
import java.util.Map;

public class SearchResult {

	private String name;
	private String descriptionUi;
	private Integer reviewCount;
	private Double avgTotal;
	private String pdfKey;
	// polje -> lista snippet-a sa <em>...</em> markup-om
	private Map<String, List<String>> highlights;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescriptionUi() { return descriptionUi; }
	public void setDescriptionUi(String descriptionUi) { this.descriptionUi = descriptionUi; }

	public Integer getReviewCount() { return reviewCount; }
	public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

	public Double getAvgTotal() { return avgTotal; }
	public void setAvgTotal(Double avgTotal) { this.avgTotal = avgTotal; }

	public String getPdfKey() { return pdfKey; }
	public void setPdfKey(String pdfKey) { this.pdfKey = pdfKey; }

	public Map<String, List<String>> getHighlights() { return highlights; }
	public void setHighlights(Map<String, List<String>> highlights) { this.highlights = highlights; }
}
