package com.wwf.shrimp.application.models;

public class Screening extends IdentifiableEntity {
	private long healthScore;
	private String testDate;
	private String floorNumber;
	private String stationId;
	private String username;
	
	
	
	public long getHealthScore() {
		return healthScore;
	}
	public void setHealthScore(long healthScore) {
		this.healthScore = healthScore;
	}
	public String getTestDate() {
		return testDate;
	}
	public void setTestDate(String testDate) {
		this.testDate = testDate;
	}

	public String getFloorNumber() {
		return floorNumber;
	}
	public void setFloorNumber(String floorNumber) {
		this.floorNumber = floorNumber;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (healthScore ^ (healthScore >>> 32));
		result = prime * result + ((testDate == null) ? 0 : testDate.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Screening other = (Screening) obj;
		if (healthScore != other.healthScore)
			return false;
		if (testDate == null) {
			if (other.testDate != null)
				return false;
		} else if (!testDate.equals(other.testDate))
			return false;
		return true;
	}	
	
	

}
