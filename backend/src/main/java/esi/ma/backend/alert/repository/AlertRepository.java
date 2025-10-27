package esi.ma.backend.alert.repository;
import esi.ma.backend.alert.model.AlertType;
import esi.ma.backend.alert.model.WeatherAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<WeatherAlert, Long> {

    List<WeatherAlert> findByActiveTrue();

    List<WeatherAlert> findByLocationKeyAndActiveTrue(String locationKey);

    List<WeatherAlert> findByTypeAndActiveTrue(AlertType type);

    Page<WeatherAlert> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<WeatherAlert> findByLocationKeyAndActiveTrueOrderByCreatedAtDesc(
            String locationKey, Pageable pageable
    );

    @Query("SELECT a FROM WeatherAlert a WHERE a.active = true AND " +
            "a.latitude BETWEEN :minLat AND :maxLat AND " +
            "a.longitude BETWEEN :minLon AND :maxLon " +
            "ORDER BY a.severity DESC, a.createdAt DESC")
    List<WeatherAlert> findActiveAlertsInBounds(
            double minLat, double maxLat,
            double minLon, double maxLon
    );

    @Query("SELECT a FROM WeatherAlert a WHERE a.expiresAt < :now")
    List<WeatherAlert> findExpiredAlerts(LocalDateTime now);

    @Query("SELECT COUNT(a) FROM WeatherAlert a WHERE a.active = true")
    long countActiveAlerts();

    @Query("SELECT COUNT(a) FROM WeatherAlert a WHERE a.active = true AND a.severity >= :severity")
    long countActiveAlertsBySeverity(int severity);
}