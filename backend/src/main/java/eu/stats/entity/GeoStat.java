package eu.stats.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "geo_stats")
public class GeoStat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false)
	private LocalDate statDate;
	
	@Column(nullable = false, length = 2)
	private String country;
	
	@Column(nullable = false)
	private Integer visits;
	
	@Column(nullable = false)
	private Integer uniqueVisitors;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getSiteId() {
		return siteId;
	}
	
	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}
	
	public LocalDate getStatDate() {
		return statDate;
	}
	
	public void setStatDate(LocalDate statDate) {
		this.statDate = statDate;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public Integer getVisits() {
		return visits;
	}
	
	public void setVisits(Integer visits) {
		this.visits = visits;
	}
	
	public Integer getUniqueVisitors() {
		return uniqueVisitors;
	}
	
	public void setUniqueVisitors(Integer uniqueVisitors) {
		this.uniqueVisitors = uniqueVisitors;
	}
}
