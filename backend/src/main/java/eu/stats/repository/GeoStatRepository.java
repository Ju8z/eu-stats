package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.GeoStat;
import eu.stats.repository.projection.TopGeoProjection;

public interface GeoStatRepository extends JpaRepository<GeoStat, Long> {
	@Query("""
			SELECT
			    g.country AS country,
			    CAST(SUM(g.visits) AS long) AS visits,
			    CAST(SUM(g.uniqueVisitors) AS long) AS uniqueVisitors
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
