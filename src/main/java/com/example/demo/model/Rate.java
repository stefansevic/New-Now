package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Rate {

	private Integer soundAndLightning;
	private Integer venue;
	private Integer overallImpression;

	@Id
	private Integer performance;

	public Integer getSoundAndLightning() {
		return soundAndLightning;
	}

	public void setSoundAndLightning(Integer soundAndLightning) {
		this.soundAndLightning = soundAndLightning;
	}

	public Integer getVenue() {
		return venue;
	}

	public void setVenue(Integer venue) {
		this.venue = venue;
	}

	public Integer getOverallImpression() {
		return overallImpression;
	}

	public void setOverallImpression(Integer overallImpression) {
		this.overallImpression = overallImpression;
	}

	public Integer getPerformance() {
		return performance;
	}

	public void setPerformance(Integer performance) {
		this.performance = performance;
	}
}


