package esi.ma.backend.weather.controller;

import esi.ma.backend.common.dto.ApiResponse;
import esi.ma.backend.location.model.Location;
import esi.ma.backend.weather.model.WeatherData;
import esi.ma.backend.weather.model.WeatherForecast;

import esi.ma.backend.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather/current?lat=35.6762&lon=139.6503
     * GET /api/weather/current?city=Tokyo
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<WeatherData>> getCurrentWeather(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String city
    ) {
        try {
            WeatherData weather;

            if (city != null && !city.isEmpty()) {
                weather = weatherService.getCurrentWeatherByCity(city);
            } else if (lat != null && lon != null) {
                weather = weatherService.getCurrentWeather(lat, lon);
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Either lat/lon or city must be provided"));
            }

            return ResponseEntity.ok(ApiResponse.success(weather));

        } catch (Exception e) {
            log.error("Error fetching weather", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch weather data: " + e.getMessage()));
        }
    }

    /**
     * GET /api/weather/forecast?lat=35.6762&lon=139.6503&days=7
     */
    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<List<WeatherForecast>>> getForecast(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "7") int days
    ) {
        try {
            if (days < 1 || days > 14) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Days must be between 1 and 14"));
            }

            List<WeatherForecast> forecast = weatherService.getForecast(lat, lon, days);
            return ResponseEntity.ok(ApiResponse.success(forecast));

        } catch (Exception e) {
            log.error("Error fetching forecast", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch forecast: " + e.getMessage()));
        }
    }

    /**
     * GET /api/weather/hourly?lat=35.6762&lon=139.6503&hours=24
     */
    @GetMapping("/hourly")
    public ResponseEntity<ApiResponse<List<WeatherForecast>>> getHourlyForecast(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "24") int hours
    ) {
        try {
            List<WeatherForecast> hourly = weatherService.getHourlyForecast(lat, lon, hours);
            return ResponseEntity.ok(ApiResponse.success(hourly));

        } catch (Exception e) {
            log.error("Error fetching hourly forecast", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch hourly forecast: " + e.getMessage()));
        }
    }

    /**
     * GET /api/weather/historical?lat=35.6762&lon=139.6503&start=2024-01-01&end=2024-01-31
     */
    @GetMapping("/historical")
    public ResponseEntity<ApiResponse<List<WeatherData>>> getHistoricalWeather(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        try {
            List<WeatherData> historical = weatherService.getHistoricalWeather(lat, lon, start, end);
            return ResponseEntity.ok(ApiResponse.success(historical));

        } catch (Exception e) {
            log.error("Error fetching historical weather", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch historical data: " + e.getMessage()));
        }
    }

    /**
     * GET /api/weather/search?q=Paris
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Location>>> searchLocations(
            @RequestParam String q
    ) {
        try {
            List<esi.ma.backend.location.model.Location> locations = weatherService.searchLocations(q);
            return ResponseEntity.ok(ApiResponse.success(locations));

        } catch (Exception e) {
            log.error("Error searching locations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search locations: " + e.getMessage()));
        }
    }
}