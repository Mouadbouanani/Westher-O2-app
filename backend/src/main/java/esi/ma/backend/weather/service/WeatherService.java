package esi.ma.backend.weather.service;

import esi.ma.backend.location.model.Location;
import esi.ma.backend.location.service.GeocodingService;
import esi.ma.backend.weather.model.WeatherData;
import esi.ma.backend.weather.model.WeatherForecast;
import esi.ma.backend.weather.repository.WeatherDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class WeatherService {

    private final WeatherApiService weatherApiService;
    private final WeatherDataRepository weatherDataRepository;
    private final WeatherCacheService cacheService;
    private final GeocodingService geocodingService;

    public WeatherService(WeatherApiService weatherApiService, WeatherDataRepository weatherDataRepository, WeatherCacheService cacheService, GeocodingService geocodingService) {
        this.weatherApiService = weatherApiService;
        this.weatherDataRepository = weatherDataRepository;
        this.cacheService = cacheService;
        this.geocodingService = geocodingService;
    }

    /**
     * Get current weather by coordinates with caching
     */
    public WeatherData getCurrentWeather(double lat, double lon) {
        String locationKey = createLocationKey(lat, lon);

        // Try cache first
        WeatherData cached = cacheService.getCachedWeather(locationKey);
        if (cached != null) {
            log.info("Returning cached weather for {}", locationKey);
            return cached;
        }

        // Fetch from API
        WeatherData weatherData = weatherApiService.fetchWeatherData(lat, lon);

        // Save to database
        weatherData.setLocationKey(locationKey);
        weatherData.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        weatherDataRepository.save(weatherData);

        // Cache it
        cacheService.cacheWeather(locationKey, weatherData);

        return weatherData;
    }

    /**
     * Get current weather by city name
     */
    public WeatherData getCurrentWeatherByCity(String cityName) {
        // Geocode city to coordinates
        Location location = geocodingService.geocode(cityName);
        return getCurrentWeather(location.getLatitude(), location.getLongitude());
    }

    /**
     * Get weather forecast for multiple days
     */
    public List<WeatherForecast> getForecast(double lat, double lon, int days) {
        return weatherApiService.fetchForecast(lat, lon, days);
    }

    /**
     * Get hourly forecast
     */
    public List<WeatherForecast> getHourlyForecast(double lat, double lon, int hours) {
        return weatherApiService.fetchHourlyForecast(lat, lon, hours);
    }

    /**
     * Get historical weather data
     */
    public List<WeatherData> getHistoricalWeather(
            double lat, double lon,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String locationKey = createLocationKey(lat, lon);
        return weatherDataRepository.findByLocationKeyAndTimestampBetween(
                locationKey, startDate, endDate
        );
    }

    /**
     * Search locations by name
     */
    public List<esi.ma.backend.location.model.Location> searchLocations(String query) {
        return geocodingService.search(query);
    }

    private String createLocationKey(double lat, double lon) {
        return String.format("%.2f,%.2f", lat, lon);
    }
}