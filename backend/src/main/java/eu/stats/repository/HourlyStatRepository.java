package eu.stats.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.stats.entity.HourlyStat;

public interface HourlyStatRepository extends JpaRepository<HourlyStat, Long> {
	
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
