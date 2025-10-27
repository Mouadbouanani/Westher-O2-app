

package esi.ma.backend.weather.repository;
import esi.ma.backend.weather.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    Optional<WeatherData> findFirstByLocationKeyOrderByTimestampDesc(String locationKey);

    List<WeatherData> findByLocationKeyAndTimestampBetween(
            String locationKey,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT w FROM WeatherData w WHERE w.locationKey = :locationKey " +
            "AND w.timestamp >= :threshold ORDER BY w.timestamp DESC")
    List<WeatherData> findRecentByLocationKey(String locationKey, LocalDateTime threshold);

    void deleteByTimestampBefore(LocalDateTime threshold);
}