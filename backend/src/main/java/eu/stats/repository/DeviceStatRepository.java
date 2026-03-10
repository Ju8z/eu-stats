package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.DeviceStat;
import eu.stats.repository.projection.DeviceBreakdownProjection;

public interface DeviceStatRepository extends JpaRepository<DeviceStat, Long> {
	
	@Query("""
			SELECT
			    d.deviceType AS deviceType,
			    d.browser AS browser,
			    d.browserVersion AS browserVersion,
			    d.os AS os,
			    d.osVersion AS osVersion,
			    CAST(SUM(d.visits) AS long) AS visits
			FROM DeviceStat d
			WHERE d.siteId = :siteId
			  AND d.statDate BETWEEN :fromDate AND :toDate
			GROUP BY d.deviceType, d.browser, d.browserVersion, d.os, d.osVersion
			""")
	List<DeviceBreakdownProjection> findBreakdown(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
}
