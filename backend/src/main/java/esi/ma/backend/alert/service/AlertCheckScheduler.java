package esi.ma.backend.alert.service;


import esi.ma.backend.airquality.model.AirQuality;
import esi.ma.backend.airquality.model.O2Data;
import esi.ma.backend.airquality.service.AirQualityService;
import esi.ma.backend.airquality.service.O2CalculationService;
import esi.ma.backend.alert.model.AlertType;
import esi.ma.backend.alert.model.WeatherAlert;
import esi.ma.backend.location.model.LocationStats;
import esi.ma.backend.location.repository.LocationStatsRepository;
import esi.ma.backend.weather.model.WeatherData;
import esi.ma.backend.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertCheckScheduler {

    private final AlertService alertService;
    private final WeatherService weatherService;
    private final AirQualityService airQualityService;
    private final O2CalculationService o2CalculationService;
    private final LocationStatsRepository locationStatsRepository;

    // Temperature thresholds
    private static final double EXTREME_HIGH_TEMP = 40.0; // Celsius
    private static final double HIGH_TEMP = 35.0;
    private static final double EXTREME_LOW_TEMP = -20.0;
    private static final double LOW_TEMP = -10.0;

    // AQI thresholds
    private static final int UNHEALTHY_AQI = 150;
    private static final int VERY_UNHEALTHY_AQI = 200;
    private static final int HAZARDOUS_AQI = 300;

    /**
     * Check for weather alerts every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void checkForWeatherAlerts() {
        log.info("Starting weather alert check");

        // Get top 100 most popular locations
        List<LocationStats> popularLocations = locationStatsRepository
                .findTop10ByOrderBySearchCountDesc();

        for (LocationStats location : popularLocations) {
            try {
                checkLocationForAlerts(location);
            } catch (Exception e) {
                log.error("Error checking alerts for location {}",
                        location.getLocationKey(), e);
            }
        }

        log.info("Weather alert check completed");
    }

    /**
     * Cleanup expired alerts daily
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM
    public void cleanupExpiredAlerts() {
        log.info("Cleaning up expired alerts");

        // This would need implementation in repository
        // For now, just log
        log.info("Expired alerts cleanup completed");
    }

    /**
     * Check a specific location for alert conditions
     */
    private void checkLocationForAlerts(LocationStats location) {double lat = location.getLatitude();
        double lon = location.getLongitude();
        String locationKey = location.getLocationKey();

        // Check weather alerts
        checkTemperatureAlerts(lat, lon, locationKey, location.getCityName());

        // Check air quality alerts
        checkAirQualityAlerts(lat, lon, locationKey, location.getCityName());

        // Check O2 alerts
        checkO2Alerts(lat, lon, locationKey, location.getCityName());
    }

    /**
     * Check for temperature-related alerts
     */
    private void checkTemperatureAlerts(double lat, double lon, String locationKey, String cityName) {
        try {
            WeatherData weather = weatherService.getCurrentWeather(lat, lon);
            double temp = weather.getTemperature();

            // Check for extreme high temperature
            if (temp >= EXTREME_HIGH_TEMP) {
                createTemperatureAlert(
                        locationKey, cityName, lat, lon,
                        AlertType.EXTREME_TEMPERATURE,
                        "Extreme Heat Warning",
                        String.format("Temperature has reached %.1f°C. Extreme heat can be dangerous.", temp),
                        "Stay indoors, drink plenty of water, avoid physical exertion, and check on vulnerable individuals.",
                        temp, 5
                );
            } else if (temp >= HIGH_TEMP) {
                createTemperatureAlert(
                        locationKey, cityName, lat, lon,
                        AlertType.HIGH_TEMPERATURE,
                        "High Temperature Alert",
                        String.format("Temperature is %.1f°C. Heat advisory in effect.", temp),
                        "Limit outdoor activities during peak heat hours. Stay hydrated.",
                        temp, 3
                );
            }

            // Check for extreme low temperature
            if (temp <= EXTREME_LOW_TEMP) {
                createTemperatureAlert(
                        locationKey, cityName, lat, lon,
                        AlertType.EXTREME_TEMPERATURE,
                        "Extreme Cold Warning",
                        String.format("Temperature has dropped to %.1f°C. Dangerous cold conditions.", temp),
                        "Stay indoors, dress warmly in layers if you must go outside, and check on vulnerable individuals.",
                        temp, 5
                );
            } else if (temp <= LOW_TEMP) {
                createTemperatureAlert(
                        locationKey, cityName, lat, lon,
                        AlertType.LOW_TEMPERATURE,
                        "Low Temperature Alert",
                        String.format("Temperature is %.1f°C. Cold weather advisory.", temp),
                        "Dress warmly and limit time outdoors.",
                        temp, 3
                );
            }

            // Check for high wind
            if (weather.getWindSpeed() != null && weather.getWindSpeed() >= 15.0) { // ~55 km/h
                WeatherAlert windAlert = WeatherAlert.builder()
                        .locationKey(locationKey)
                        .cityName(cityName)
                        .latitude(lat)
                        .longitude(lon)
                        .type(AlertType.HIGH_WIND)
                        .severity(3)
                        .title("High Wind Warning")
                        .description(String.format("Wind speed has reached %.1f m/s (%.0f km/h). Strong winds may cause hazards.",
                                weather.getWindSpeed(), weather.getWindSpeed() * 3.6))
                        .recommendation("Secure loose objects outdoors. Be cautious when driving, especially high-profile vehicles.")
                        .triggerValue(weather.getWindSpeed())
                        .triggerUnit("m/s")
                        .source("SYSTEM")
                        .expiresAt(LocalDateTime.now().plusHours(12))
                        .build();

                // Only create if doesn't exist recently
                if (!recentAlertExists(locationKey, AlertType.HIGH_WIND)) {
                    alertService.createAlert(windAlert);
                    log.info("High wind alert created for {}", cityName);
                }
            }

        } catch (Exception e) {
            log.error("Error checking temperature alerts for {}", locationKey, e);
        }
    }

    /**
     * Check for air quality alerts
     */
    private void checkAirQualityAlerts(double lat, double lon, String locationKey, String cityName) {
        try {
            AirQuality airQuality = airQualityService.getAirQuality(lat, lon);
            int aqi = airQuality.getAqi();

            if (aqi >= HAZARDOUS_AQI) {
                WeatherAlert alert = WeatherAlert.builder()
                        .locationKey(locationKey)
                        .cityName(cityName)
                        .latitude(lat)
                        .longitude(lon)
                        .type(AlertType.HAZARDOUS_AIR)
                        .severity(5)
                        .title("Hazardous Air Quality")
                        .description(String.format("Air quality has reached hazardous levels (AQI: %d). Health emergency conditions.", aqi))
                        .recommendation("Everyone should avoid all outdoor activities. Remain indoors with air filtration if possible. " +
                                "Wear N95 masks if outdoor exposure is unavoidable.")
                        .triggerValue((double) aqi)
                        .triggerUnit("AQI")
                        .source("SYSTEM")
                        .expiresAt(LocalDateTime.now().plusHours(6))
                        .build();

                if (!recentAlertExists(locationKey, AlertType.HAZARDOUS_AIR)) {
                    alertService.createAlert(alert);
                    log.info("Hazardous air quality alert created for {}", cityName);
                }
            } else if (aqi >= VERY_UNHEALTHY_AQI) {
                WeatherAlert alert = WeatherAlert.builder()
                        .locationKey(locationKey)
                        .cityName(cityName)
                        .latitude(lat)
                        .longitude(lon)
                        .type(AlertType.VERY_UNHEALTHY_AIR)
                        .severity(4)
                        .title("Very Unhealthy Air Quality")
                        .description(String.format("Air quality is very unhealthy (AQI: %d). Health alert.", aqi))
                        .recommendation("Avoid all outdoor physical activities. Everyone should stay indoors. " +
                                "Sensitive groups should keep windows closed.")
                        .triggerValue((double) aqi)
                        .triggerUnit("AQI")
                        .source("SYSTEM")
                        .expiresAt(LocalDateTime.now().plusHours(6))
                        .build();

                if (!recentAlertExists(locationKey, AlertType.VERY_UNHEALTHY_AIR)) {
                    alertService.createAlert(alert);
                    log.info("Very unhealthy air quality alert created for {}", cityName);
                }
            } else if (aqi >= UNHEALTHY_AQI) {
                WeatherAlert alert = WeatherAlert.builder()
                        .locationKey(locationKey)
                        .cityName(cityName)
                        .latitude(lat)
                        .longitude(lon)
                        .type(AlertType.POOR_AIR_QUALITY)
                        .severity(3)
                        .title("Unhealthy Air Quality")
                        .description(String.format("Air quality is unhealthy (AQI: %d). Sensitive groups at risk.", aqi))
                        .recommendation("Sensitive groups should avoid prolonged outdoor activities. " +
                                "Everyone should reduce outdoor exertion.")
                        .triggerValue((double) aqi)
                        .triggerUnit("AQI")
                        .source("SYSTEM")
                        .expiresAt(LocalDateTime.now().plusHours(6))
                        .build();

                if (!recentAlertExists(locationKey, AlertType.POOR_AIR_QUALITY)) {
                    alertService.createAlert(alert);
                    log.info("Poor air quality alert created for {}", cityName);
                }
            }

        } catch (Exception e) {
            log.error("Error checking air quality alerts for {}", locationKey, e);
        }
    }

    /**
     * Check for O2 level alerts
     */
    private void checkO2Alerts(double lat, double lon, String locationKey, String cityName) {
        try {
            O2Data o2Data = o2CalculationService.getO2DataForLocation(lat, lon);
            String healthLevel = o2Data.getHealthLevel();
            double o2Concentration = o2Data.getO2Concentration();

            if ("DANGEROUS".equals(healthLevel)) {
                WeatherAlert alert = WeatherAlert.builder()
                        .locationKey(locationKey)
                        .cityName(cityName)
                        .latitude(lat)
                        .longitude(lon)
                        .type(AlertType.DANGEROUS_OXYGEN)
                        .severity(5)
                        .title("Dangerous Oxygen Levels")
                        .description(String.format("Oxygen concentration is dangerously low (%.2f%%). Risk of severe hypoxia.",
                                o2Concentration))
                        .recommendation("Immediate descent to lower altitude recommended. Seek medical attention if experiencing symptoms. " +
                                "Do not engage in any physical activity.")
                        .triggerValue(o2Concentration)
                        .triggerUnit("%")
                        .source("SYSTEM")
                        .expiresAt(LocalDateTime.now().plusHours(12))
                        .build();

                if (!recentAlertExists(locationKey, AlertType.DANGEROUS_OXYGEN)) {
                    alertService.createAlert(alert);
                    log.info("Dangerous oxygen alert created for {}", cityName);
                }
            } else if ("VERY_LOW".equals(healthLevel)) {
                WeatherAlert alert = WeatherAlert.builder()
                        .locationKey(locationKey)
                        .cityName(cityName)
                        .latitude(lat)
                        .longitude(lon)
                        .type(AlertType.LOW_OXYGEN)
                        .severity(4)
                        .title("Low Oxygen Levels")
                        .description(String.format("Oxygen concentration is significantly reduced (%.2f%%). Altitude effects likely.",
                                o2Concentration))
                        .recommendation("Physical exertion not recommended. People with heart or lung conditions should avoid this altitude. " +
                                "Allow time for acclimatization.")
                        .triggerValue(o2Concentration)
                        .triggerUnit("%")
                        .source("SYSTEM")
                        .expiresAt(LocalDateTime.now().plusHours(12))
                        .build();

                if (!recentAlertExists(locationKey, AlertType.LOW_OXYGEN)) {
                    alertService.createAlert(alert);
                    log.info("Low oxygen alert created for {}", cityName);
                }
            }

        } catch (Exception e) {
            log.error("Error checking O2 alerts for {}", locationKey, e);
        }
    }

    /**
     * Helper method to create temperature alert
     */
    private void createTemperatureAlert(
            String locationKey, String cityName,
            double lat, double lon,
            AlertType type, String title, String description,
            String recommendation, double temp, int severity
    ) {
        if (!recentAlertExists(locationKey, type)) {
            WeatherAlert alert = WeatherAlert.builder()
                    .locationKey(locationKey)
                    .cityName(cityName)
                    .latitude(lat)
                    .longitude(lon)
                    .type(type)
                    .severity(severity)
                    .title(title)
                    .description(description)
                    .recommendation(recommendation)
                    .triggerValue(temp)
                    .triggerUnit("°C")
                    .source("SYSTEM")
                    .expiresAt(LocalDateTime.now().plusHours(12))
                    .build();

            alertService.createAlert(alert);
            log.info("{} alert created for {}", type, cityName);
        }
    }

    /**
     * Check if a recent alert already exists (within last 6 hours)
     */
    private boolean recentAlertExists(String locationKey, AlertType type) {
        List<WeatherAlert> existingAlerts = alertService.getActiveAlertsForLocation(
                parseLatitude(locationKey),
                parseLongitude(locationKey)
        );

        LocalDateTime threshold = LocalDateTime.now().minusHours(6);

        return existingAlerts.stream()
                .anyMatch(alert -> alert.getType() == type &&
                        alert.getCreatedAt().isAfter(threshold));
    }

    private double parseLatitude(String locationKey) {
        return Double.parseDouble(locationKey.split(",")[0]);
    }

    private double parseLongitude(String locationKey) {
        return Double.parseDouble(locationKey.split(",")[1]);
    }
}