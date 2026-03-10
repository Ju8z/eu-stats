package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.GeoStat;
import eu.stats.repository.projection.TopGeoProjection;

/**
 * Keeps geographic statistics query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface GeoStatRepository extends JpaRepository<GeoStat, Long> {
	/**
	 * Reads already aggregated country totals.
	 * Keeping country ranking in the repository lets the service add display labels without repeating grouping
	 * logic.
	 *
	 * @param siteId   site identifier
	 * @param fromDate start date
	 * @param toDate   end date
	 * @return ranked country rows
	 */
	@Query("""
			SELECT
			    g.country AS country,
			    CAST(SUM(g.visits) AS long) AS visits
			FROM GeoStat g
			WHERE g.siteId = :siteId
			  AND g.statDate BETWEEN :fromDate AND :toDate
			GROUP BY g.country
			ORDER BY SUM(g.visits) DESC
			""")
	List<TopGeoProjection> findTopCountries(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
}
