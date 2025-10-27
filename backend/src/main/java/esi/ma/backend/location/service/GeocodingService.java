package esi.ma.backend.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import esi.ma.backend.common.exception.InvalidRequestException;
import esi.ma.backend.location.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodingService {

    @Value("${geocoding.api.url:https://geocoding-api.open-meteo.com/v1}")
    private String geocodingApiUrl;

    private final RestTemplate restTemplate;

    /**
     * Search locations by name
     */
    @Cacheable(value = "locations", key = "#query")
    public List<Location> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidRequestException("Search query cannot be empty");
        }

        String url = String.format("%s/search?name=%s&count=10&language=en&format=json",
                geocodingApiUrl, query);

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode body = response.getBody();

            if (body == null || !body.has("results")) {
                return new ArrayList<>();
            }

            List<Location> locations = new ArrayList<>();
            JsonNode results = body.get("results");

            for (JsonNode result : results) {
                Location location = Location.builder()
                        .name(result.get("name").asText())
                        .cityName(result.get("name").asText())
                        .countryName(result.has("country") ? result.get("country").asText() : null)
                        .countryCode(result.has("country_code") ? result.get("country_code").asText() : null)
                        .state(result.has("admin1") ? result.get("admin1").asText() : null)
                        .latitude(result.get("latitude").asDouble())
                        .longitude(result.get("longitude").asDouble())
                        .population(result.has("population") ? result.get("population").asInt() : null)
                        .timezone(result.has("timezone") ? result.get("timezone").asText() : null)
                        .build();

                locations.add(location);
            }

            return locations;

        } catch (Exception e) {
            log.error("Failed to search locations for query: {}", query, e);
            throw new InvalidRequestException("Failed to search locations: " + e.getMessage());
        }
    }

    /**
     * Reverse geocode coordinates to location
     */
    @Cacheable(value = "locations", key = "#lat + ',' + #lon")
    public Location reverseGeocode(double lat, double lon) {
        // Use Nominatim for reverse geocoding
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?lat=%.6f&lon=%.6f&format=json",
                lat, lon
        );

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode body = response.getBody();

            if (body == null) {
                throw new InvalidRequestException("Failed to reverse geocode location");
            }

            JsonNode address = body.get("address");

            return Location.builder()
                    .cityName(getCityName(address))
                    .countryName(address.has("country") ? address.get("country").asText() : null)
                    .countryCode(address.has("country_code") ? address.get("country_code").asText() : null)
                    .state(address.has("state") ? address.get("state").asText() : null)
                    .latitude(lat)
                    .longitude(lon)
                    .build();

        } catch (Exception e) {
            log.error("Failed to reverse geocode {},{}", lat, lon, e);
            // Return basic location with just coordinates
            return Location.builder()
                    .cityName("Unknown Location")
                    .latitude(lat)
                    .longitude(lon)
                    .build();
        }
    }

    /**
     * Get city name from address node
     */
    private String getCityName(JsonNode address) {
        if (address.has("city")) {
            return address.get("city").asText();
        } else if (address.has("town")) {
            return address.get("town").asText();
        } else if (address.has("village")) {
            return address.get("village").asText();
        } else if (address.has("municipality")) {
            return address.get("municipality").asText();
        }
        return "Unknown";
    }

    public Location geocode(String cityName) {
        List<Location> results = search(cityName);
        if (results.isEmpty()) {
            throw new InvalidRequestException("Location not found for city: " + cityName);
        }
        return results.get(0);
    }
}