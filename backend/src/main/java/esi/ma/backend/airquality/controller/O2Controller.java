package esi.ma.backend.airquality.controller;


import esi.ma.backend.airquality.dto.AltitudeEffectsInfo;
import esi.ma.backend.airquality.dto.O2CalculationResult;
import esi.ma.backend.airquality.dto.O2Response;
import esi.ma.backend.airquality.model.O2Data;
import esi.ma.backend.airquality.service.O2CalculationService;
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
@RequestMapping("/api/o2")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class O2Controller {

    private final O2CalculationService o2CalculationService;

    /**
     * GET /api/o2/current?lat=48.8566&lon=2.3522
     * Get current O2 concentration for location
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<O2Response>> getCurrentO2Data(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);

            O2Data o2Data = o2CalculationService.getO2DataForLocation(lat, lon);
            O2Response response = O2Response.from(o2Data);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error fetching O2 data", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch O2 data: " + e.getMessage()));
        }
    }

    /**
     * GET /api/o2/calculate?altitude=1500&pressure=850&temperature=15&humidity=60
     * Calculate O2 concentration with custom parameters
     */
    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<O2CalculationResult>> calculateO2(
            @RequestParam Integer altitude,
            @RequestParam Double pressure,
            @RequestParam Double temperature,
            @RequestParam Double humidity
    ) {
        try {
            ValidationUtil.validateRange(altitude, 0, 9000, "Altitude");
            ValidationUtil.validateRange(pressure, 500, 1100, "Pressure");
            ValidationUtil.validateRange(temperature, -50, 60, "Temperature");
            ValidationUtil.validateRange(humidity, 0, 100, "Humidity");

            double o2Concentration = o2CalculationService.calculateO2Concentration(
                    altitude, pressure, temperature, humidity
            );

            String healthLevel = o2CalculationService.classifyO2Level(o2Concentration);
            String recommendation = o2CalculationService.getHealthRecommendation(o2Concentration);
            double bloodO2 = o2CalculationService.estimateBloodO2Saturation(altitude, o2Concentration);

            O2CalculationResult result = O2CalculationResult.builder()
                    .o2Concentration(o2Concentration)
                    .healthLevel(healthLevel)
                    .recommendation(recommendation)
                    .estimatedBloodO2Saturation(bloodO2)
                    .altitude(altitude)
                    .pressure(pressure)
                    .temperature(temperature)
                    .humidity(humidity)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("Error calculating O2", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to calculate O2: " + e.getMessage()));
        }
    }

    /**
     * GET /api/o2/historical?lat=48.8566&lon=2.3522&start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     * Get historical O2 data
     */
    @GetMapping("/historical")
    public ResponseEntity<ApiResponse<List<O2Data>>> getHistoricalO2Data(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            ValidationUtil.validateCoordinates(lat, lon);
            ValidationUtil.validateDateRange(start, end, 90);

            List<O2Data> historicalData = o2CalculationService.getHistoricalO2Data(
                    lat, lon, start, end
            );

            return ResponseEntity.ok(ApiResponse.success(historicalData));

        } catch (Exception e) {
            log.error("Error fetching historical O2 data", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch historical data"));
        }
    }

    /**
     * GET /api/o2/altitude-effects?altitude=3000
     * Get information about altitude effects on O2
     */
    @GetMapping("/altitude-effects")
    public ResponseEntity<ApiResponse<AltitudeEffectsInfo>> getAltitudeEffects(
            @RequestParam Integer altitude
    ) {
        try {
            ValidationUtil.validateRange(altitude, 0, 9000, "Altitude");

            AltitudeEffectsInfo info = o2CalculationService.getAltitudeEffectsInfo(altitude);
            return ResponseEntity.ok(ApiResponse.success(info));

        } catch (Exception e) {
            log.error("Error getting altitude effects", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get altitude effects"));
        }
    }
}