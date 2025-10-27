package esi.ma.backend.crowdsourcing.repository;

import esi.ma.backend.crowdsourcing.model.EnvironmentalReport;
import esi.ma.backend.crowdsourcing.model.ReportCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends org.springframework.data.jpa.repository.JpaRepository<EnvironmentalReport, Long> {
    List<EnvironmentalReport> findTopByCreatedAtAfterOrderByUpvotesDesc(LocalDateTime since, PageRequest of);

    @Query("SELECT e FROM EnvironmentalReport e WHERE e.category = :category AND e.latitude BETWEEN :swLat AND :neLat AND e.longitude BETWEEN :swLon AND :neLon")
    List<EnvironmentalReport> findByCategoryAndLocationWithinBounds(@Param("category") ReportCategory category, 
                                                                   @Param("swLat") double swLat, @Param("neLat") double neLat,
                                                                   @Param("swLon") double swLon, @Param("neLon") double neLon);

    @Query("SELECT e FROM EnvironmentalReport e WHERE e.latitude BETWEEN :swLat AND :neLat AND e.longitude BETWEEN :swLon AND :neLon")
    List<EnvironmentalReport> findByLocationWithinBounds(@Param("swLat") double swLat, @Param("neLat") double neLat,
                                                        @Param("swLon") double swLon, @Param("neLon") double neLon);

    @Query("SELECT e FROM EnvironmentalReport e WHERE e.latitude BETWEEN :minLat AND :maxLat AND e.longitude BETWEEN :minLon AND :maxLon AND e.hidden = false")
    Page<EnvironmentalReport> findByLocationWithinBoundsAndNotHidden(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                                                                    @Param("minLon") double minLon, @Param("maxLon") double maxLon,
                                                                    Pageable pageable);

    @Query("SELECT e FROM EnvironmentalReport e WHERE e.category = :category AND e.latitude BETWEEN :minLat AND :maxLat AND e.longitude BETWEEN :minLon AND :maxLon AND e.hidden = false")
    Page<EnvironmentalReport> findByCategoryAndLocationWithinBoundsAndNotHidden(@Param("category") ReportCategory category,
                                                                              @Param("minLat") double minLat, @Param("maxLat") double maxLat,
                                                                              @Param("minLon") double minLon, @Param("maxLon") double maxLon,
                                                                              Pageable pageable);

    int countByIpHashAndCreatedAtAfter(String ipHash, LocalDateTime localDateTime);
    
    EnvironmentalReport save(EnvironmentalReport report);

    Optional<EnvironmentalReport> findById(Long id);

    long countByVerifiedTrue();

    long countByCreatedAtAfter(LocalDateTime dateTime);
}

