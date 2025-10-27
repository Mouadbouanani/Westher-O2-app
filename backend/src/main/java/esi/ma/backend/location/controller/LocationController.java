package esi.ma.backend.location.controller;



import esi.ma.backend.common.dto.ApiResponse;
import esi.ma.backend.common.util.ValidationUtil;
import esi.ma.backend.location.model.Location;
import esi.ma.backend.location.model.LocationStats;
import esi.ma.backend.location.service.GeocodingService;
import esi.ma.backend.location.service.PopularLocationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class LocationController {

    private final GeocodingService geocodingService;
    private final PopularLocationsService popularLocationsService;

    /**
     * GET /api/locations/search?q=Paris
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<esi.ma.backend.location.model.Location>>> searchLocations(
            @RequestParam String q
    ) {
        try {
            ValidationUtil.validateNotEmpty(q, "Search query");

            List<esi.ma.backend.location.model.Location> locations = geocodingService.search(q);
            return ResponseEntity.ok(ApiResponse.success(locations));

        } catch (Exception e) {
            log.error("Error searching locations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search locations: " + e.getMessage()));
        }
    }

    /**
     * GET /api/locations/reverse?lat=48.8566&lon=2.3522
     */
    @GetMapping("/reverse")
    public ResponseEntity<ApiResponse<Location>> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);
            Location location = geocodingService.reverseGeocode(lat, lon);
            return ResponseEntity.ok(ApiResponse.success(location));

        } catch (Exception e) {
            log.error("Error reverse geocoding", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to reverse geocode: " + e.getMessage()));
        }
    }

    /**
     * GET /api/locations/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<LocationStats>>> getPopularLocations(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            ValidationUtil.validateRange(limit, 1, 50, "Limit");

            List<LocationStats> popular = popularLocationsService.getPopularLocations(limit);
            return ResponseEntity.ok(ApiResponse.success(popular));

        } catch (Exception e) {
            log.error("Error getting popular locations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get popular locations"));
        }
    }

    /**
     * GET /api/locations/trending?limit=10
     */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<LocationStats>>> getTrendingLocations(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            ValidationUtil.validateRange(limit, 1, 50, "Limit");

            List<LocationStats> trending = popularLocationsService.getTrendingLocations(limit);
            return ResponseEntity.ok(ApiResponse.success(trending));

        } catch (Exception e) {
            log.error("Error getting trending locations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get trending locations"));
        }
    }

    /**
     * GET /api/locations/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<LocationStats>>> getRecentLocations(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            ValidationUtil.validateRange(limit, 1, 50, "Limit");

            List<LocationStats> recent = popularLocationsService.getRecentLocations(limit);
            return ResponseEntity.ok(ApiResponse.success(recent));

        } catch (Exception e) {
            log.error("Error getting recent locations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get recent locations"));
        }
    }

    /**
     * GET /api/locations/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PopularLocationsService.GlobalLocationStats>> getGlobalStats() {
        try {
            PopularLocationsService.GlobalLocationStats stats =
                    popularLocationsService.getGlobalStats();
            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            log.error("Error getting global stats", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get global stats"));
        }
    }
}