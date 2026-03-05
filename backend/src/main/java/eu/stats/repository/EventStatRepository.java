package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.EventStat;
import eu.stats.repository.projection.EventProjection;

public interface EventStatRepository extends JpaRepository<EventStat, Long> {
	
	@Query("""
			SELECT
			    e.eventName AS eventName,
			    CAST(SUM(e.eventCount) AS long) AS eventCount,
			    CAST(SUM(e.uniqueVisitors) AS long) AS uniqueVisitors
			FROM EventStat e
			WHERE e.siteId = :siteId
			  AND e.statDate BETWEEN :fromDate AND :toDate
			GROUP BY e.eventName
			ORDER BY SUM(e.eventCount) DESC
			""")
	List<EventProjection> findEvents(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
}
