package eu.stats.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "daily_stats")
public class DailyStat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false)
	private LocalDate statDate;
	
	@Column(nullable = false)
	private Integer totalPageviews;
	
	@Column(nullable = false)
	private Integer uniqueVisitors;
	
	@Column(nullable = false)
	private Integer totalSessions;
	
	@Column(nullable = false)
	private Integer bounceCount;
	
	@Column(nullable = false)
	private Long totalSessionDurationSeconds;
	
	@Column(nullable = false)
	private Integer totalPageDepth;
	
	@Column(nullable = false)
	private OffsetDateTime createdAt;
	
	public DailyStat() {
	}
	
	public DailyStat(Long id, Long siteId, LocalDate statDate, Integer totalPageviews, Integer uniqueVisitors,
			Integer totalSessions, Integer bounceCount, Long totalSessionDurationSeconds, Integer totalPageDepth,
			OffsetDateTime createdAt) {
		this.id = id;
		this.siteId = siteId;
		this.statDate = statDate;
		this.totalPageviews = totalPageviews;
		this.uniqueVisitors = uniqueVisitors;
		this.totalSessions = totalSessions;
		this.bounceCount = bounceCount;
		this.totalSessionDurationSeconds = totalSessionDurationSeconds;
		this.totalPageDepth = totalPageDepth;
		this.createdAt = createdAt;
	}
	
	@PrePersist
	public void onPrePersist() {
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}
	
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
	
	public Integer getTotalSessions() {
		return totalSessions;
	}
	
	public void setTotalSessions(Integer totalSessions) {
		this.totalSessions = totalSessions;
	}
	
	public Integer getBounceCount() {
		return bounceCount;
	}
	
	public void setBounceCount(Integer bounceCount) {
		this.bounceCount = bounceCount;
	}
	
	public Long getTotalSessionDurationSeconds() {
		return totalSessionDurationSeconds;
	}
	
	public void setTotalSessionDurationSeconds(Long totalSessionDurationSeconds) {
		this.totalSessionDurationSeconds = totalSessionDurationSeconds;
	}
	
	public Integer getTotalPageDepth() {
		return totalPageDepth;
	}
	
	public void setTotalPageDepth(Integer totalPageDepth) {
		this.totalPageDepth = totalPageDepth;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
