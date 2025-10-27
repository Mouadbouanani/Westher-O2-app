package esi.ma.backend.alert.controller;
import esi.ma.backend.alert.model.AlertType;
import esi.ma.backend.alert.model.WeatherAlert;
import esi.ma.backend.alert.service.AlertService;
import esi.ma.backend.common.dto.ApiResponse;
import esi.ma.backend.common.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts?lat=48.8566&lon=2.3522
     * Get active alerts for a specific location
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WeatherAlert>>> getAlertsForLocation(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);

            List<WeatherAlert> alerts = alertService.getActiveAlertsForLocation(lat, lon);
            return ResponseEntity.ok(ApiResponse.success(alerts));

        } catch (Exception e) {
            log.error("Error getting alerts for location", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get alerts: " + e.getMessage()));
        }
    }

    /**
     * GET /api/alerts/all?page=0&size=20
     * Get all active alerts (paginated)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<WeatherAlert>>> getAllActiveAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            ValidationUtil.validatePagination(page, size);

            Pageable pageable = PageRequest.of(page, size);
            Page<WeatherAlert> alerts = alertService.getActiveAlerts(pageable);

            return ResponseEntity.ok(ApiResponse.success(alerts));

        } catch (Exception e) {
            log.error("Error getting all alerts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get alerts"));
        }
    }

    /**
     * GET /api/alerts/map?neLat=49&neLon=3&swLat=48&swLon=2
     * Get alerts in bounding box for map view
     */
    @GetMapping("/map")
    public ResponseEntity<ApiResponse<List<WeatherAlert>>> getAlertsForMap(
            @RequestParam Double neLat,
            @RequestParam Double neLon,
            @RequestParam Double swLat,
            @RequestParam Double swLon
    ) {
        try {
            ValidationUtil.validateCoordinates(neLat, neLon);
            ValidationUtil.validateCoordinates(swLat, swLon);

            List<WeatherAlert> alerts = alertService.getActiveAlertsInBounds(
                    neLat, neLon, swLat, swLon
            );

            return ResponseEntity.ok(ApiResponse.success(alerts));

        } catch (Exception e) {
            log.error("Error getting alerts for map", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get alerts"));
        }
    }

    /**
     * GET /api/alerts/type/{type}
     * Get alerts by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<WeatherAlert>>> getAlertsByType(
            @PathVariable AlertType type
    ) {
        try {
            List<WeatherAlert> alerts = alertService.getActiveAlertsByType(type);
            return ResponseEntity.ok(ApiResponse.success(alerts));

        } catch (Exception e) {
            log.error("Error getting alerts by type", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get alerts"));
        }
    }

    /**
     * GET /api/alerts/{id}
     * Get specific alert by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeatherAlert>> getAlertById(
            @PathVariable Long id
    ) {
        try {
            // Would need to implement in service
            return ResponseEntity.ok(ApiResponse.success(null));

        } catch (Exception e) {
            log.error("Error getting alert by ID", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get alert"));
        }
    }

    /**
     * GET /api/alerts/stats
     * Get alert statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AlertService.AlertStatistics>> getAlertStats() {
        try {
            AlertService.AlertStatistics stats = alertService.getStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            log.error("Error getting alert statistics", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get statistics"));
        }
    }
}