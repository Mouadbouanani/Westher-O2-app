package esi.ma.backend.airquality.repository;

import esi.ma.backend.airquality.model.AirQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AirQualityRepository extends JpaRepository<AirQuality, Long> {

    Optional<AirQuality> findFirstByLocationKeyAndTimestampAfterOrderByTimestampDesc(
            String locationKey,
            LocalDateTime threshold
    );

    List<AirQuality> findByLocationKeyAndTimestampBetween(
            String locationKey,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT aq FROM AirQuality aq WHERE " +
            "aq.latitude BETWEEN :minLat AND :maxLat AND " +
            "aq.longitude BETWEEN :minLon AND :maxLon AND " +
            "aq.timestamp >= :threshold " +
            "ORDER BY aq.timestamp DESC")
    List<AirQuality> findRecentByLocationBounds(
            double minLat, double maxLat,
            double minLon, double maxLon,
            LocalDateTime threshold
    );

    long countByAqiGreaterThan(int aqi);

    @Query("SELECT AVG(aq.aqi) FROM AirQuality aq WHERE aq.timestamp >= :since")
    Double getAverageAqiSince(LocalDateTime since);
}