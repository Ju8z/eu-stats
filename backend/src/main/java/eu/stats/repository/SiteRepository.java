package eu.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.stats.entity.Site;

public interface SiteRepository extends JpaRepository<Site, Long> {
}
