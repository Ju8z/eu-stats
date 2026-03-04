package eu.stats.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.DailyStat;
import eu.stats.repository.projection.OverviewAggregateProjection;

public interface DailyStatRepository extends JpaRepository<DailyStat, Long> {
	
	@Query("""
			SELECT
			    CAST(SUM(d.totalPageviews) AS long) AS totalPageviews,
			    CAST(SUM(d.uniqueVisitors) AS long) AS uniqueVisitors
			FROM DailyStat d
			WHERE d.siteId = :siteId
			  AND d.statDate BETWEEN :fromDate AND :toDate
			""")
	OverviewAggregateProjection summarizeRange(@Param("siteId") Long siteId,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);
	
	List<DailyStat> findBySiteIdAndStatDateBetweenOrderByStatDateAsc(Long siteId, LocalDate fromDate, LocalDate toDate);
}
