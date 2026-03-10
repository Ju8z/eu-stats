package eu.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.stats.entity.Site;

/**
 * Keeps site query rules at the storage boundary.
 * That lets services talk in analytics terms while database-specific grouping, sorting, and conflict
 * handling stay close to PostgreSQL.
 */
public interface SiteRepository extends JpaRepository<Site, Long> {
}
