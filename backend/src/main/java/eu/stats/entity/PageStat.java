package eu.stats.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "page_stats")
public class PageStat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false)
	private LocalDate statDate;
	
	@Column(nullable = false, length = 2048)
	private String pageUrl;
	
	@Column(length = 500)
	private String pageTitle;
	
	@Column(nullable = false)
	private Integer pageviews;
	
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
	
	public String getPageUrl() {
		return pageUrl;
	}
	
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	public String getPageTitle() {
		return pageTitle;
	}
	
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	
	public Integer getPageviews() {
		return pageviews;
	}
	
	public void setPageviews(Integer pageviews) {
		this.pageviews = pageviews;
	}
	
	public Integer getUniqueVisitors() {
		return uniqueVisitors;
	}
	
	public void setUniqueVisitors(Integer uniqueVisitors) {
		this.uniqueVisitors = uniqueVisitors;
	}
}
