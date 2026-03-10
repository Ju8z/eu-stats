package eu.stats.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Keeps persistence mapping explicit for page view data.
 * The project uses handwritten entities so stored fields and lifecycle behavior remain easy to audit
 * without code generation.
 */
@Entity
@Table(name = "pageviews")
public class PageView {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long siteId;
	
	@Column(nullable = false, length = 64)
	private String visitorHash;
	
	@Column(nullable = false, length = 2048)
	private String pageUrl;
	
	@Column(length = 500)
	private String pageTitle;
	
	@Column(length = 500)
	private String referrer;
	
	@Column(length = 20)
	private String referrerCategory;
	
	@Column(length = 100)
	private String browser;
	
	@Column(length = 20)
	private String browserVersion;
	
	@Column(length = 100)
	private String os;
	
	@Column(length = 20)
	private String osVersion;
	
	@Column(length = 20)
	private String deviceType;

	@Column(length = 2)
	private String country;
	
	@Column(nullable = false, length = 50)
	private String eventType;
	
	private String eventName;
	
	@Column(nullable = false)
	private OffsetDateTime viewedAt;
	
	@Column(nullable = false)
	private OffsetDateTime createdAt;
	
	public PageView() {
	}
	
	public PageView(Long id, Long siteId, String visitorHash, String pageUrl, String pageTitle,
			String referrer, String referrerCategory, String browser, String browserVersion, String os, String osVersion, String deviceType,
			String country,
			String eventType, String eventName, OffsetDateTime viewedAt,
			OffsetDateTime createdAt) {
		this.id = id;
		this.siteId = siteId;
		this.visitorHash = visitorHash;
		this.pageUrl = pageUrl;
		this.pageTitle = pageTitle;
		this.referrer = referrer;
		this.referrerCategory = referrerCategory;
		this.browser = browser;
		this.browserVersion = browserVersion;
		this.os = os;
		this.osVersion = osVersion;
		this.deviceType = deviceType;
		this.country = country;
		this.eventType = eventType;
		this.eventName = eventName;
		this.viewedAt = viewedAt;
		this.createdAt = createdAt;
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
	
	public String getVisitorHash() {
		return visitorHash;
	}
	
	public void setVisitorHash(String visitorHash) {
		this.visitorHash = visitorHash;
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
	
	public String getBrowser() {
		return browser;
	}
	
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	
	public String getBrowserVersion() {
		return browserVersion;
	}
	
	public void setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
	}
	
	public String getOs() {
		return os;
	}
	
	public void setOs(String os) {
		this.os = os;
	}
	
	public String getOsVersion() {
		return osVersion;
	}
	
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	
	public String getDeviceType() {
		return deviceType;
	}
	
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getEventType() {
		return eventType;
	}
	
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
	public String getEventName() {
		return eventName;
	}
	
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public OffsetDateTime getViewedAt() {
		return viewedAt;
	}
	
	public void setViewedAt(OffsetDateTime viewedAt) {
		this.viewedAt = viewedAt;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
