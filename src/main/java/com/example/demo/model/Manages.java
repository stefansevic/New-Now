package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

@Entity
@IdClass(Manages.ManagesId.class)
public class Manages {

	@Id
	@ManyToOne
	@JoinColumn(name = "user_email")
	private User user;

	@Id
	@ManyToOne
	@JoinColumn(name = "location_name")
	private Location location;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	// Composite key class
	public static class ManagesId {
		private String user;
		private String location;

		public ManagesId() {}

		public ManagesId(String user, String location) {
			this.user = user;
			this.location = location;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ManagesId managesId = (ManagesId) o;
			return Objects.equals(user, managesId.user) &&
				   Objects.equals(location, managesId.location);
		}

		@Override
		public int hashCode() {
			return Objects.hash(user, location);
		}
	}
}


