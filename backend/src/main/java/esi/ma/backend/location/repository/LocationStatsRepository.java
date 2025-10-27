package esi.ma.backend.location.repository;


import esi.ma.backend.location.model.LocationStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationStatsRepository extends JpaRepository<LocationStats, Long> {

    Optional<LocationStats> findByLocationKey(String locationKey);

    List<LocationStats> findTop10ByOrderBySearchCountDesc();

    List<LocationStats> findTop10ByOrderByLastAccessedAtDesc();

    @Query("SELECT ls FROM LocationStats ls WHERE ls.lastAccessedAt >= :since " +
            "ORDER BY ls.searchCount DESC")
    List<LocationStats> findTrendingLocations(LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(ls) FROM LocationStats ls")
    long countTotalLocations();

    @Query("SELECT SUM(ls.searchCount) FROM LocationStats ls")
    long sumTotalSearches();
}