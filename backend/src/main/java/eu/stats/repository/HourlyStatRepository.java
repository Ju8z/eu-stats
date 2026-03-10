package eu.stats.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.HourlyStat;

/**
 * Keeps hourly statistics query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface HourlyStatRepository extends JpaRepository<HourlyStat, Long> {
	
	/**
	 * Reads pre-aggregated hourly rows for short-range charts.
	 * Using hourly summaries keeps recent charts responsive without rebuilding every hour from raw tracker
	 * events.
	 *
	 * @param siteId        site identifier
	 * @param fromInclusive inclusive start timestamp
	 * @param toExclusive   exclusive end timestamp
	 * @return hourly statistics rows ordered by hour
	 */
	@Query("""
			SELECT h
			FROM HourlyStat h
			WHERE h.siteId = :siteId
			  AND h.statHour >= :fromInclusive
			  AND h.statHour < :toExclusive
			ORDER BY h.statHour ASC
			""")
	List<HourlyStat> findBySiteIdAndStatHourBetweenOrderByStatHourAsc(
			@Param("siteId") Long siteId,
			@Param("fromInclusive") OffsetDateTime fromInclusive,
			@Param("toExclusive") OffsetDateTime toExclusive);
}
