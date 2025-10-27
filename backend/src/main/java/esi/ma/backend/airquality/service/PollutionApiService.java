package esi.ma.backend.airquality.service;
import com.fasterxml.jackson.databind.JsonNode;
import esi.ma.backend.airquality.model.AirQuality;
import esi.ma.backend.common.exception.PollutionApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class PollutionApiService {

    @Value("${pollution.api.openaq.url}")
    private String openAqUrl; // https://api.openaq.org/v2

    @Value("${pollution.api.waqi.url}")
    private String waqiUrl; // https://api.waqi.info

    @Value("${pollution.api.waqi.token}")
    private String waqiToken;

    private final RestTemplate restTemplate;

    /**
     * Fetch pollution data with fallback
     */
    @CircuitBreaker(name = "pollutionApi", fallbackMethod = "fetchPollutionFallback")
    public AirQuality fetchPollutionData(double lat, double lon) {
        try {
            return fetchFromWAQI(lat, lon);
        } catch (Exception e) {
            log.warn("WAQI failed, trying OpenAQ", e);
            return fetchFromOpenAQ(lat, lon);
        }
    }

    /**
     * Fetch from World Air Quality Index API
     */
    private AirQuality fetchFromWAQI(double lat, double lon) {
        String url = String.format(
                "%s/feed/geo:%.2f;%.2f/?token=%s",
                waqiUrl, lat, lon, waqiToken
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapWAQIResponse(response.getBody(), lat, lon);
    }

    /**
     * Fetch from OpenAQ API
     */
    private AirQuality fetchFromOpenAQ(double lat, double lon) {
        String url = String.format(
                "%s/latest?coordinates=%.2f,%.2f&radius=25000&limit=1",
                openAqUrl, lat, lon
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapOpenAQResponse(response.getBody(), lat, lon);
    }

    private AirQuality mapOpenAQResponse(JsonNode body, double lat, double lon) {
        JsonNode results = body.get("results");
        if (results.isEmpty()) {
            throw new PollutionApiException("No data from OpenAQ");
        }

        JsonNode locationData = results.get(0);
        AirQuality aq = new AirQuality();
        aq.setLatitude(lat);
        aq.setLongitude(lon);
        aq.setCityName(locationData.get("city").asText());
        aq.setLocationKey(String.format("%.2f,%.2f", lat, lon));

        for (JsonNode measurement : locationData.get("measurements")) {
            String parameter = measurement.get("parameter").asText();
            double value = measurement.get("value").asDouble();

            switch (parameter) {
                case "pm25" -> aq.setPm25(value);
                case "pm10" -> aq.setPm10(value);
                case "o3" -> aq.setO3(value);
                case "no2" -> aq.setNo2(value);
                case "so2" -> aq.setSo2(value);
                case "co" -> aq.setCo(value);
            }
        }

        // Note: OpenAQ does not provide AQI directly
        aq.setDataSource("OpenAQ");
        return aq;
    }

    private AirQuality mapWAQIResponse(JsonNode json, double lat, double lon) {
        JsonNode data = json.get("data");

        AirQuality aq = new AirQuality();
        aq.setLatitude(lat);
        aq.setLongitude(lon);
        aq.setCityName(data.get("city").get("name").asText());
        aq.setLocationKey(String.format("%.2f,%.2f", lat, lon));

        // WAQI returns AQI directly
        aq.setAqi(data.get("aqi").asInt());

        // Extract individual pollutants
        JsonNode iaqi = data.get("iaqi");
        if (iaqi.has("pm25")) aq.setPm25(iaqi.get("pm25").get("v").asDouble());
        if (iaqi.has("pm10")) aq.setPm10(iaqi.get("pm10").get("v").asDouble());
        if (iaqi.has("o3")) aq.setO3(iaqi.get("o3").get("v").asDouble());
        if (iaqi.has("no2")) aq.setNo2(iaqi.get("no2").get("v").asDouble());
        if (iaqi.has("so2")) aq.setSo2(iaqi.get("so2").get("v").asDouble());
        if (iaqi.has("co")) aq.setCo(iaqi.get("co").get("v").asDouble());

        aq.setDataSource("WAQI");
        return aq;
    }

    // Fallback when all APIs fail
    private AirQuality fetchPollutionFallback(double lat, double lon, Exception e) {
        log.error("All pollution APIs failed", e);
        throw new PollutionApiException("Pollution data unavailable");
    }
}