package eu.stats.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hourly_stats")
public class HourlyStat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false)
	private OffsetDateTime statHour;
	
	@Column(nullable = false)
	private Integer totalPageviews;
	
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
	
	public OffsetDateTime getStatHour() {
		return statHour;
	}
	
	public void setStatHour(OffsetDateTime statHour) {
		this.statHour = statHour;
	}
	
	public Integer getTotalPageviews() {
		return totalPageviews;
	}
	
	public void setTotalPageviews(Integer totalPageviews) {
		this.totalPageviews = totalPageviews;
	}
	
	public Integer getUniqueVisitors() {
		return uniqueVisitors;
	}
	
	public void setUniqueVisitors(Integer uniqueVisitors) {
		this.uniqueVisitors = uniqueVisitors;
	}
}
