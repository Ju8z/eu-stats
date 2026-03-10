package eu.stats.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Keeps persistence mapping explicit for referrer statistics data.
 * The project uses handwritten entities so stored fields and lifecycle behavior remain easy to audit
 * without code generation.
 */
@Entity
@Table(name = "referrer_stats")
public class ReferrerStat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false)
	private LocalDate statDate;
	
	@Column(nullable = false, length = 500)
	private String referrer;
	
	@Column(length = 20)
	private String referrerCategory;
	
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
	
	public String getReferrer() {
		return referrer;
	}
	
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
	
	public String getReferrerCategory() {
		return referrerCategory;
	}
	
	public void setReferrerCategory(String referrerCategory) {
		this.referrerCategory = referrerCategory;
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
