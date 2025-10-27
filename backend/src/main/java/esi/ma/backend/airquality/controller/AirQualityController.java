package esi.ma.backend.airquality.controller;


import esi.ma.backend.airquality.dto.AirQualityMapPoint;
import esi.ma.backend.airquality.dto.AirQualityResponse;
import esi.ma.backend.airquality.dto.PollutantBreakdown;
import esi.ma.backend.airquality.model.AirQuality;
import esi.ma.backend.airquality.service.AirQualityService;
import esi.ma.backend.common.dto.ApiResponse;
import esi.ma.backend.common.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/airquality")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class AirQualityController {

    private final AirQualityService airQualityService;

    /**
     * GET /api/airquality/current?lat=48.8566&lon=2.3522
     * Get current air quality for location
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<AirQualityResponse>> getCurrentAirQuality(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);

            AirQuality airQuality = airQualityService.getAirQuality(lat, lon);
            AirQualityResponse response = AirQualityResponse.from(airQuality);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error fetching air quality", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch air quality: " + e.getMessage()));
        }
    }

    /**
     * GET /api/airquality/historical?lat=48.8566&lon=2.3522&start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     * Get historical air quality data
     */
    @GetMapping("/historical")
    public ResponseEntity<ApiResponse<List<AirQuality>>> getHistoricalAirQuality(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);
            ValidationUtil.validateDateRange(start, end, 90); // Max 90 days

            List<AirQuality> historicalData = airQualityService.getHistoricalAirQuality(
                    lat, lon, start, end
            );

            return ResponseEntity.ok(ApiResponse.success(historicalData));

        } catch (Exception e) {
            log.error("Error fetching historical air quality", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch historical data: " + e.getMessage()));
        }
    }

    /**
     * GET /api/airquality/map?neLat=49&neLon=3&swLat=48&swLon=2
     * Get air quality data for map heatmap
     */
    @GetMapping("/map")
    public ResponseEntity<ApiResponse<List<AirQualityMapPoint>>> getAirQualityForMap(
            @RequestParam Double neLat,
            @RequestParam Double neLon,
            @RequestParam Double swLat,
            @RequestParam Double swLon
    ) {
        try {
            ValidationUtil.validateCoordinates(neLat, neLon);
            ValidationUtil.validateCoordinates(swLat, swLon);
            List<AirQualityMapPoint> mapData = airQualityService.getAirQualityForMapBounds(
                    neLat, neLon, swLat, swLon
            );

            return ResponseEntity.ok(ApiResponse.success(mapData));

        } catch (Exception e) {
            log.error("Error fetching air quality for map", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch map data"));
        }
    }

    /**
     * GET /api/airquality/pollutants?lat=48.8566&lon=2.3522
     * Get detailed pollutant breakdown
     */
    @GetMapping("/pollutants")
    public ResponseEntity<ApiResponse<PollutantBreakdown>> getPollutantBreakdown(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);

            PollutantBreakdown breakdown = airQualityService.getPollutantBreakdown(lat, lon);
            return ResponseEntity.ok(ApiResponse.success(breakdown));

        } catch (Exception e) {
            log.error("Error fetching pollutant breakdown", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch pollutant data"));
        }
    }
}