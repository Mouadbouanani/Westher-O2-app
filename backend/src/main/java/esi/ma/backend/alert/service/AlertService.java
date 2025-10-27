package esi.ma.backend.alert.service;
import esi.ma.backend.alert.model.AlertType;
import esi.ma.backend.alert.model.WeatherAlert;
import esi.ma.backend.alert.repository.AlertRepository;
import esi.ma.backend.common.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    /**
     * Create a new alert
     */
    @Transactional
    public WeatherAlert createAlert(WeatherAlert alert) {
        alert.setActive(true);

        // Set default expiration if not provided (24 hours)
        if (alert.getExpiresAt() == null) {
            alert.setExpiresAt(LocalDateTime.now().plusHours(24));
        }

        return alertRepository.save(alert);
    }

    /**
     * Get active alerts for a location
     */
    public List<WeatherAlert> getActiveAlertsForLocation(double lat, double lon) {
        String locationKey = GeoUtil.createLocationKey(lat, lon);
        return alertRepository.findByLocationKeyAndActiveTrue(locationKey);
    }

    /**
     * Get all active alerts (paginated)
     */
    public Page<WeatherAlert> getActiveAlerts(Pageable pageable) {
        return alertRepository.findByActiveTrueOrderByCreatedAtDesc(pageable);
    }

    /**
     * Get active alerts in bounding box
     */
    public List<WeatherAlert> getActiveAlertsInBounds(
            double neLat, double neLon,
            double swLat, double swLon
    ) {
        return alertRepository.findActiveAlertsInBounds(swLat, neLat, swLon, neLon);
    }

    /**
     * Get active alerts by type
     */
    public List<WeatherAlert> getActiveAlertsByType(AlertType type) {
        return alertRepository.findByTypeAndActiveTrue(type);
    }

    /**
     * Deactivate an alert
     */
    @Transactional
    public void deactivateAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setActive(false);
            alertRepository.save(alert);
            log.info("Alert {} deactivated", alertId);
        });
    }

    /**
     * Get alert statistics
     */
    public AlertStatistics getStatistics() {
        long totalActive = alertRepository.countActiveAlerts();
        long highSeverity = alertRepository.countActiveAlertsBySeverity(4);
        long criticalSeverity = alertRepository.countActiveAlertsBySeverity(5);

        return AlertStatistics.builder()
                .totalActiveAlerts(totalActive)
                .highSeverityAlerts(highSeverity)
                .criticalAlerts(criticalSeverity)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class AlertStatistics {
        private long totalActiveAlerts;
        private long highSeverityAlerts;
        private long criticalAlerts;
    }
}