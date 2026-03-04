package eu.stats.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.stats.entity.HourlyStat;

public interface HourlyStatRepository extends JpaRepository<HourlyStat, Long> {
	
	List<HourlyStat> findBySiteIdAndStatHourBetweenOrderByStatHourAsc(
			Long siteId,
			OffsetDateTime from,
			OffsetDateTime to);
}
