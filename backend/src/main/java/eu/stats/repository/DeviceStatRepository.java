package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.DeviceStat;
import eu.stats.repository.projection.DeviceBreakdownProjection;

/**
 * Keeps device statistics query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface DeviceStatRepository extends JpaRepository<DeviceStat, Long> {
	
	/**
	 * Reads the pre-aggregated device and technology breakdown.
	 * The service can then reshape those rows for the dashboard without querying raw page views or carrying
	 * browser columns that the current dashboard does not render.
	 *
	 * @param siteId   site identifier
	 * @param fromDate start date
	 * @param toDate   end date
	 * @return device breakdown rows
	 */
	@Query("""
			SELECT
			    d.deviceType AS deviceType,
			    d.os AS os,
			    d.osVersion AS osVersion,
			    CAST(SUM(d.visits) AS long) AS visits
			FROM DeviceStat d
			WHERE d.siteId = :siteId
			  AND d.statDate BETWEEN :fromDate AND :toDate
			GROUP BY d.deviceType, d.os, d.osVersion
			""")
	List<DeviceBreakdownProjection> findBreakdown(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
}
