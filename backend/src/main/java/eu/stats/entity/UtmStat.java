package eu.stats.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "utm_stats")
public class UtmStat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false)
	private LocalDate statDate;
	
	@Column(nullable = false)
	private Integer visits;
	
	@Column(nullable = false)
	private Integer uniqueVisitors;
	
	private String utmSource;
	private String utmMedium;
	private String utmCampaign;
	
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
	
	public String getUtmSource() {
		return utmSource;
	}
	
	public void setUtmSource(String utmSource) {
		this.utmSource = utmSource;
	}
	
	public String getUtmMedium() {
		return utmMedium;
	}
	
	public void setUtmMedium(String utmMedium) {
		this.utmMedium = utmMedium;
	}
	
	public String getUtmCampaign() {
		return utmCampaign;
	}
	
	public void setUtmCampaign(String utmCampaign) {
		this.utmCampaign = utmCampaign;
	}
}
