package esi.ma.backend.weather.service;

import com.fasterxml.jackson.databind.JsonNode;


import com.fasterxml.jackson.databind.ObjectMapper;
import esi.ma.backend.airquality.model.AirQuality;
import esi.ma.backend.common.exception.WeatherApiException;
import esi.ma.backend.location.model.Location;
import esi.ma.backend.weather.model.WeatherData;
import esi.ma.backend.weather.model.WeatherForecast;
import esi.ma.backend.weather.repository.WeatherDataRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherApiService {

    @Value("${weather.api.weatherapi.url}")
    private String weatherApiUrl;

    @Value("${weather.api.weatherapi.key}")
    private String weatherApiKey;

    @Value("${weather.api.openmeteo.url}")
    private String openMeteoUrl;

    @Value("${weather.api.openweather.key}")
    private String openWeatherApiKey;

    @Value("${weather.api.openweather.url}")
    private String openWeatherUrl;

    @Value("${weather.api.tomorrow.url}")
    private String tomorrowApiUrl;

    @Value("${weather.api.tomorrow.key}")
    private String tomorrowApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherDataRepository weatherDataRepository;


    public List<WeatherForecast> fetchHourlyForecast(double lat, double lon, int hours) {
        try {
            // Try WeatherAPI hourly forecast
            return fetchHourlyForecastFromWeatherApi(lat, lon, hours);
        } catch (Exception e) {
            log.warn("WeatherAPI hourly forecast failed, trying Tomorrow.io", e);
            try {
                return fetchHourlyForecastFromTomorrowApi(lat, lon, hours);
            } catch (Exception e2) {
                log.warn("Tomorrow.io hourly forecast failed, trying OpenWeatherMap", e2);
                return fetchHourlyForecastFromOpenWeather(lat, lon, hours);
            }
        }
    }
    private List<WeatherForecast> mapHourlyForecastResponse(JsonNode json, double lat, double lon) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        // Parse hourly data from JSON
        // ... implementation
        return forecasts;
    }

    private AirQuality mapOpenAQResponse(JsonNode json, double lat, double lon) {
        AirQuality aq = new AirQuality();
        aq.setLatitude(lat);
        aq.setLongitude(lon);
        // Parse OpenAQ response
        // ... implementation
        return aq;
    }

    /**
     * Fetch weather with automatic fallback
     */
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fetchWeatherFallback")
    @Retry(name = "weatherApi")
    public WeatherData fetchWeatherData(double lat, double lon) {
        try {
            // Primary: WeatherAPI
            return fetchFromWeatherApi(lat, lon);
        } catch (Exception e) {
            log.warn("WeatherAPI failed, trying Tomorrow.io", e);
            try {
                return fetchFromTomorrowApi(lat, lon);
            } catch (Exception e2) {
                log.warn("Tomorrow.io failed, trying OpenWeatherMap", e2);
                return fetchFromOpenWeather(lat, lon);
            }
        }
    }

    /**
     * Fetch from Open-Meteo API
     */
    private WeatherData fetchFromOpenMeteo(double lat, double lon) {
        String url = String.format(
                "%s/v1/forecast?" +
                        "latitude=%.2f&longitude=%.2f&" +
                        "current_weather=true&" +
                        "hourly=temperature_2m,relative_humidity_2m,dew_point_2m," +
                        "apparent_temperature,precipitation,pressure_msl,cloud_cover," +
                        "wind_speed_10m,wind_direction_10m,uv_index&" +
                        "timezone=auto",
                openMeteoUrl, lat, lon
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapOpenMeteoResponse(response.getBody(), lat, lon);
    }

    /**
     * Fetch from WeatherAPI
     */
    private WeatherData fetchFromWeatherApi(double lat, double lon) {
        String url = String.format(
                "%s/current.json?" +
                        "key=%s&" +
                        "q=%.2f,%.2f&" +
                        "aqi=no",
                weatherApiUrl, weatherApiKey, lat, lon
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapWeatherApiResponse(response.getBody(), lat, lon);
    }

    /**
     * Fetch from OpenWeatherMap API
     */
    private WeatherData fetchFromOpenWeather(double lat, double lon) {
        String url = String.format(
                "%s/data/2.5/weather?" +
                        "lat=%.2f&lon=%.2f&" +
                        "appid=%s&units=metric",
                openWeatherUrl, lat, lon, openWeatherApiKey
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapOpenWeatherResponse(response.getBody(), lat, lon);
    }

    /**
     * Fetch from Tomorrow.io API
     */
    private WeatherData fetchFromTomorrowApi(double lat, double lon) {
        String url = String.format(
                "%s/weather/realtime?" +
                        "location=%.2f,%.2f&" +
                        "apikey=%s",
                tomorrowApiUrl, tomorrowApiKey, lat, lon
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapTomorrowApiResponse(response.getBody(), lat, lon);
    }

    /**
     * Fetch forecast from API with fallback
     */
    public List<WeatherForecast> fetchForecast(double lat, double lon, int days) {
        try {
            // Try WeatherAPI forecast
            return fetchForecastFromWeatherApi(lat, lon, days);
        } catch (Exception e) {
            log.warn("WeatherAPI forecast failed, trying Tomorrow.io", e);
            try {
                return fetchForecastFromTomorrowApi(lat, lon, days);
            } catch (Exception e2) {
                log.warn("Tomorrow.io forecast failed, trying OpenWeatherMap", e2);
                return fetchForecastFromOpenWeather(lat, lon, days);
            }
        }
    }

    /**
     * Fallback method when all APIs fail
     */
    private WeatherData fetchWeatherFallback(double lat, double lon, Exception e) {
        log.error("All weather APIs failed for location {},{}", lat, lon, e);

        // Return last known data from database
        String locationKey = String.format("%.2f,%.2f", lat, lon);
        return weatherDataRepository.findFirstByLocationKeyOrderByTimestampDesc(locationKey)
                .orElseThrow(() -> new WeatherApiException("Weather data unavailable"));
    }

    // Forecast methods for different APIs
    private List<WeatherForecast> fetchForecastFromWeatherApi(double lat, double lon, int days) {
        String url = String.format(
                "%s/forecast.json?" +
                        "key=%s&" +
                        "q=%.2f,%.2f&" +
                        "days=%d&" +
                        "aqi=no&" +
                        "alerts=no",
                weatherApiUrl, weatherApiKey, lat, lon, days
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapWeatherApiForecastResponse(response.getBody(), lat, lon);
    }

    private List<WeatherForecast> fetchForecastFromTomorrowApi(double lat, double lon, int days) {
        String url = String.format(
                "%s/weather/forecast?" +
                        "location=%.2f,%.2f&" +
                        "timesteps=1d&" +
                        "apikey=%s&" +
                        "units=metric",
                tomorrowApiUrl, lat, lon, tomorrowApiKey
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapTomorrowForecastResponse(response.getBody(), lat, lon);
    }

    private List<WeatherForecast> fetchForecastFromOpenWeather(double lat, double lon, int days) {
        // OpenWeatherMap doesn't have a separate forecast endpoint for days in free tier
        // We'll use the current weather API since OpenMeteo is removed
        String url = String.format(
                "%s/data/2.5/weather?" +
                        "lat=%.2f&lon=%.2f&" +
                        "appid=%s&units=metric",
                openWeatherUrl, lat, lon, openWeatherApiKey
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapOpenWeatherForecastResponse(response.getBody(), lat, lon, days);
    }

    // Mapping methods...
    private WeatherData mapOpenMeteoResponse(JsonNode json, double lat, double lon) {
        JsonNode current = json.get("current_weather");
        JsonNode hourly = json.get("hourly");

        WeatherData weather = new WeatherData();
        weather.setLatitude(lat);
        weather.setLongitude(lon);
        weather.setTemperature(current.get("temperature").asDouble());
        weather.setWindSpeed(current.get("windspeed").asDouble());
        weather.setWindDirection(current.get("winddirection").asDouble());
        weather.setWeatherCode(current.get("weathercode").asInt());

        // Extract hourly data for current hour
        if (hourly != null) {
            weather.setHumidity(hourly.get("relative_humidity_2m").get(0).asDouble());
            weather.setPressure(hourly.get("pressure_msl").get(0).asDouble());
            weather.setCloudCoverage(hourly.get("cloud_cover").get(0).asDouble());
            weather.setUvIndex(hourly.get("uv_index").get(0).asDouble());
            weather.setDewPoint(hourly.get("dew_point_2m").get(0).asDouble());
        }

        weather.setDataSource("OpenMeteo");
        return weather;
    }

    private WeatherData mapOpenWeatherResponse(JsonNode json, double lat, double lon) {
        WeatherData weather = new WeatherData();
        weather.setLatitude(lat);
        weather.setLongitude(lon);

        JsonNode main = json.get("main");
        weather.setTemperature(main.get("temp").asDouble()); // CORRECT
        weather.setFeelsLike(main.get("feels_like").asDouble());
        weather.setTempMin(main.get("temp_min").asDouble());
        weather.setTempMax(main.get("temp_max").asDouble());
        weather.setHumidity(main.get("humidity").asDouble());
        weather.setPressure(main.get("pressure").asDouble());

        JsonNode wind = json.get("wind");
        weather.setWindSpeed(wind.get("speed").asDouble());
        weather.setWindDirection(wind.get("deg").asDouble());

        JsonNode clouds = json.get("clouds");
        weather.setCloudCoverage(clouds.get("all").asDouble());

        if (json.has("visibility")) {
            weather.setVisibility(json.get("visibility").asDouble() / 1000.0); // Convert m to km
        }

        JsonNode weatherArray = json.get("weather").get(0);
        weather.setWeatherMain(weatherArray.get("main").asText());
        weather.setWeatherDescription(weatherArray.get("description").asText());
        weather.setIconCode(weatherArray.get("icon").asText());

        weather.setCityName(json.get("name").asText());
        weather.setCountryCode(json.get("sys").get("country").asText());

        weather.setDataSource("OpenWeatherMap");
        return weather;
    }

    // Mapping method for WeatherAPI response
    private WeatherData mapWeatherApiResponse(JsonNode json, double lat, double lon) {
        WeatherData weather = new WeatherData();
        weather.setLatitude(lat);
        weather.setLongitude(lon);

        // Extract location info
        JsonNode location = json.get("location");
        if (location != null) {
            weather.setCityName(location.get("name").asText());
            weather.setCountryCode(location.get("country").asText());
        }

        // Extract current weather data
        JsonNode current = json.get("current");
        if (current != null) {
            weather.setTemperature(current.get("temp_c").asDouble());
            weather.setFeelsLike(current.get("feelslike_c").asDouble());
            weather.setHumidity(current.get("humidity").asDouble());
            weather.setPressure(current.get("pressure_mb").asDouble() * 10); // Convert mb to hPa
            
            // Wind data
            weather.setWindSpeed(current.get("wind_kph").asDouble() / 3.6); // Convert kph to m/s
            weather.setWindDirection(current.get("wind_degree").asDouble());
            
            // Visibility (convert km to km)
            if (current.has("vis_km")) {
                weather.setVisibility(current.get("vis_km").asDouble());
            }
            
            // Cloud coverage
            if (current.has("cloud")) {
                weather.setCloudCoverage(current.get("cloud").asDouble());
            }
            
            // UV index
            if (current.has("uv")) {
                weather.setUvIndex(current.get("uv").asDouble());
            }
            
            // Condition data
            JsonNode condition = current.get("condition");
            if (condition != null) {
                weather.setWeatherMain(condition.get("text").asText());
                weather.setWeatherDescription(condition.get("text").asText());
                // Extract icon from the condition code
                if (condition.has("code")) {
                    weather.setIconCode(condition.get("code").asText());
                }
            }
            
            // Precipitation
            if (current.has("precip_mm")) {
                weather.setPrecipitation(current.get("precip_mm").asDouble());
            }
            
            // Dew point
            if (current.has("dewpoint_c")) {
                weather.setDewPoint(current.get("dewpoint_c").asDouble());
            }
        }

        weather.setDataSource("WeatherAPI");
        return weather;
    }

    // Mapping method for Tomorrow.io API response
    private WeatherData mapTomorrowApiResponse(JsonNode json, double lat, double lon) {
        WeatherData weather = new WeatherData();
        weather.setLatitude(lat);
        weather.setLongitude(lon);

        JsonNode data = json.get("data");
        if (data != null) {
            JsonNode currentTime = data.get("time");
            JsonNode values = data.get("values");
            
            if (currentTime != null) {
                weather.setCityName(currentTime.asText()); // Store time as a reference if needed
            }
            
            if (values != null) {
                // Temperature
                if (values.has("temperature")) {
                    weather.setTemperature(values.get("temperature").asDouble());
                }
                
                // Feels like temperature
                if (values.has("temperatureApparent")) {
                    weather.setFeelsLike(values.get("temperatureApparent").asDouble());
                }
                
                // Humidity
                if (values.has("humidity")) {
                    weather.setHumidity(values.get("humidity").asDouble());
                }
                
                // Pressure (typically in mb, convert to hPa)
                if (values.has("pressureSurfaceLevel")) {
                    weather.setPressure(values.get("pressureSurfaceLevel").asDouble());
                }
                
                // Wind speed and direction
                if (values.has("windSpeed")) {
                    weather.setWindSpeed(values.get("windSpeed").asDouble()); // Already in m/s
                }
                if (values.has("windDirection")) {
                    weather.setWindDirection(values.get("windDirection").asDouble());
                }
                
                // Cloud coverage
                if (values.has("cloudCover")) {
                    weather.setCloudCoverage(values.get("cloudCover").asDouble());
                }
                
                // UV index
                if (values.has("uvIndex")) {
                    weather.setUvIndex(values.get("uvIndex").asDouble());
                }
                
                // Visibility (convert meters to km)
                if (values.has("visibility")) {
                    weather.setVisibility(values.get("visibility").asDouble() / 1000.0);
                }
                
                // Precipitation
                if (values.has("precipitationIntensity")) {
                    weather.setPrecipitation(values.get("precipitationIntensity").asDouble());
                }
                
                // Dew point
                if (values.has("dewPoint")) {
                    weather.setDewPoint(values.get("dewPoint").asDouble());
                }
                
                // Weather code
                if (values.has("weatherCode")) {
                    weather.setWeatherCode(values.get("weatherCode").asInt());
                }
            }
        }

        weather.setDataSource("Tomorrow.io");
        return weather;
    }

    // Mapping methods for forecasts
    private List<WeatherForecast> mapWeatherApiForecastResponse(JsonNode json, double lat, double lon) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        JsonNode forecast = json.get("forecast");
        if (forecast != null) {
            JsonNode forecastday = forecast.get("forecastday");
            if (forecastday != null) {
                for (JsonNode dayNode : forecastday) {
                    WeatherForecast forecastDay = new WeatherForecast();
                    forecastDay.setLatitude(lat);
                    forecastDay.setLongitude(lon);
                    forecastDay.setLocationKey(String.format("%.2f,%.2f", lat, lon));
                    
                    // Parse date
                    if (dayNode.has("date")) {
                        String dateString = dayNode.get("date").asText();
                        try {
                            java.time.LocalDate date = java.time.LocalDate.parse(dateString);
                            forecastDay.setForecastDate(date.atStartOfDay());
                        } catch (Exception e) {
                            log.warn("Could not parse date: {}", dateString);
                        }
                    }
                    
                    // Day data
                    JsonNode day = dayNode.get("day");
                    if (day != null) {
                        if (day.has("avgtemp_c")) {
                            forecastDay.setTemperature(day.get("avgtemp_c").asDouble());
                        }
                        if (day.has("maxtemp_c")) {
                            forecastDay.setTempMax(day.get("maxtemp_c").asDouble());
                        }
                        if (day.has("mintemp_c")) {
                            forecastDay.setTempMin(day.get("mintemp_c").asDouble());
                        }
                        if (day.has("totalprecip_mm")) {
                            forecastDay.setPrecipitation(day.get("totalprecip_mm").asDouble());
                        }
                        if (day.has("avghumidity")) {
                            forecastDay.setHumidity(day.get("avghumidity").asDouble());
                        }
                        if (day.has("maxwind_kph")) {
                            forecastDay.setWindSpeed(day.get("maxwind_kph").asDouble() / 3.6); // Convert kph to m/s
                        }
                        
                        // Condition
                        JsonNode condition = day.get("condition");
                        if (condition != null) {
                            forecastDay.setWeatherMain(condition.get("text").asText());
                            forecastDay.setWeatherDescription(condition.get("text").asText());
                        }
                    }
                    
                    forecastDay.setForecastType(WeatherForecast.ForecastType.DAILY);
                    forecastDay.setDataSource("WeatherAPI");
                    forecasts.add(forecastDay);
                }
            }
        }
        
        return forecasts;
    }

    private List<WeatherForecast> mapTomorrowForecastResponse(JsonNode json, double lat, double lon) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        JsonNode timeline = json.get("data");
        if (timeline != null) {
            JsonNode intervals = timeline.get("timelines");
            if (intervals != null) {
                JsonNode dailyIntervals = intervals.get(0); // Assuming the first timeline is daily
                if (dailyIntervals != null) {
                    JsonNode intervalsArray = dailyIntervals.get("intervals");
                    if (intervalsArray != null) {
                        for (JsonNode interval : intervalsArray) {
                            WeatherForecast forecast = new WeatherForecast();
                            forecast.setLatitude(lat);
                            forecast.setLongitude(lon);
                            forecast.setLocationKey(String.format("%.2f,%.2f", lat, lon));
                            
                            // Parse time
                            if (interval.has("time")) {
                                String timeString = interval.get("time").asText();
                                try {
                                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timeString.replace("Z", ""));
                                    forecast.setForecastDate(dateTime);
                                } catch (Exception e) {
                                    log.warn("Could not parse time: {}", timeString);
                                }
                            }
                            
                            // Values
                            JsonNode values = interval.get("values");
                            if (values != null) {
                                if (values.has("temperatureAvg")) {
                                    forecast.setTemperature(values.get("temperatureAvg").asDouble());
                                }
                                if (values.has("temperatureMin")) {
                                    forecast.setTempMin(values.get("temperatureMin").asDouble());
                                }
                                if (values.has("temperatureMax")) {
                                    forecast.setTempMax(values.get("temperatureMax").asDouble());
                                }
                                if (values.has("precipitationIntensityAvg")) {
                                    forecast.setPrecipitation(values.get("precipitationIntensityAvg").asDouble());
                                }
                                if (values.has("humidityAvg")) {
                                    forecast.setHumidity(values.get("humidityAvg").asDouble());
                                }
                                if (values.has("windSpeedAvg")) {
                                    forecast.setWindSpeed(values.get("windSpeedAvg").asDouble());
                                }
                            }
                            
                            forecast.setForecastType(WeatherForecast.ForecastType.DAILY);
                            forecast.setDataSource("Tomorrow.io");
                            forecasts.add(forecast);
                        }
                    }
                }
            }
        }
        
        return forecasts;
    }
    
    private List<WeatherForecast> mapOpenWeatherForecastResponse(JsonNode json, double lat, double lon, int days) {
        // For compatibility, return a single forecast based on current weather
        // In a real implementation, you'd use OpenWeatherMap's One Call API or 5 Day Forecast API
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        // Create a single forecast for current weather
        WeatherForecast forecast = new WeatherForecast();
        forecast.setLatitude(lat);
        forecast.setLongitude(lon);
        forecast.setLocationKey(String.format("%.2f,%.2f", lat, lon));
        forecast.setForecastDate(java.time.LocalDateTime.now());
        
        JsonNode main = json.get("main");
        if (main != null) {
            forecast.setTemperature(main.get("temp").asDouble());
            forecast.setTempMin(main.get("temp_min").asDouble());
            forecast.setTempMax(main.get("temp_max").asDouble());
            forecast.setHumidity(main.get("humidity").asDouble());
            forecast.setPressure(main.get("pressure").asDouble());
        }
        
        JsonNode wind = json.get("wind");
        if (wind != null) {
            forecast.setWindSpeed(wind.get("speed").asDouble());
            forecast.setWindDirection(wind.get("deg").asDouble());
        }
        
        JsonNode weatherArray = json.get("weather").get(0);
        forecast.setWeatherMain(weatherArray.get("main").asText());
        forecast.setWeatherDescription(weatherArray.get("description").asText());
        
        forecast.setForecastType(WeatherForecast.ForecastType.DAILY);
        forecast.setDataSource("OpenWeatherMap");
        forecasts.add(forecast);
        
        // In a real implementation, you might repeat this for the requested number of days
        // or use a different API that provides extended forecasts
        return forecasts;
    }

    // Helper methods for hourly forecasts
    private List<WeatherForecast> fetchHourlyForecastFromWeatherApi(double lat, double lon, int hours) {
        // WeatherAPI doesn't have a dedicated hourly forecast endpoint in the same way
        // We'll use the forecast API with 1 day and extract hourly details if available
        String url = String.format(
                "%s/forecast.json?" +
                        "key=%s&" +
                        "q=%.2f,%.2f&" +
                        "days=1&" + // Use 1 day since we're looking for current/hourly
                        "aqi=no&" +
                        "alerts=no",
                weatherApiUrl, weatherApiKey, lat, lon
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapWeatherApiHourlyForecastResponse(response.getBody(), lat, lon, hours);
    }

    private List<WeatherForecast> fetchHourlyForecastFromTomorrowApi(double lat, double lon, int hours) {
        String url = String.format(
                "%s/weather/forecast?" +
                        "location=%.2f,%.2f&" +
                        "timesteps=1h&" + // Hourly timesteps
                        "apikey=%s&" +
                        "units=metric&" +
                        "startTime=now&" +
                        "endTime=+%.0fh", // Forecast for the next 'hours' hours
                tomorrowApiUrl, lat, lon, tomorrowApiKey, hours
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapTomorrowHourlyForecastResponse(response.getBody(), lat, lon, hours);
    }

    private List<WeatherForecast> fetchHourlyForecastFromOpenWeather(double lat, double lon, int hours) {
        // Using OpenWeatherMap One Call API for hourly data (though it requires a different endpoint)
        // For now, we'll return current weather data since the basic weather endpoint doesn't have hourly
        String url = String.format(
                "%s/data/2.5/weather?" +
                        "lat=%.2f&lon=%.2f&" +
                        "appid=%s&units=metric",
                openWeatherUrl, lat, lon, openWeatherApiKey
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        return mapOpenWeatherHourlyForecastResponse(response.getBody(), lat, lon, hours);
    }

    // Mapping methods for hourly forecasts
    private List<WeatherForecast> mapWeatherApiHourlyForecastResponse(JsonNode json, double lat, double lon, int hours) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        // WeatherAPI typically doesn't provide detailed hourly forecast, so returning based on current
        // In a real implementation, you'd extract hour-by-hour data if available
        WeatherForecast forecast = new WeatherForecast();
        forecast.setLatitude(lat);
        forecast.setLongitude(lon);
        forecast.setLocationKey(String.format("%.2f,%.2f", lat, lon));
        forecast.setForecastDate(java.time.LocalDateTime.now());
        forecast.setForecastType(WeatherForecast.ForecastType.HOURLY);
        forecast.setDataSource("WeatherAPI");
        
        JsonNode current = json.get("current");
        if (current != null) {
            if (current.has("temp_c")) {
                forecast.setTemperature(current.get("temp_c").asDouble());
            }
            if (current.has("feelslike_c")) {
                forecast.setFeelsLike(current.get("feelslike_c").asDouble());
            }
            if (current.has("humidity")) {
                forecast.setHumidity(current.get("humidity").asDouble());
            }
            if (current.has("pressure_mb")) {
                forecast.setPressure(current.get("pressure_mb").asDouble() * 10); // Convert mb to hPa
            }
            if (current.has("wind_kph")) {
                forecast.setWindSpeed(current.get("wind_kph").asDouble() / 3.6); // Convert kph to m/s
            }
            if (current.has("wind_degree")) {
                forecast.setWindDirection(current.get("wind_degree").asDouble());
            }
        }
        
        forecasts.add(forecast);
        
        // In a complete implementation, you would generate forecasts for each hour up to 'hours'
        return forecasts;
    }

    private List<WeatherForecast> mapTomorrowHourlyForecastResponse(JsonNode json, double lat, double lon, int hours) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        JsonNode data = json.get("data");
        if (data != null) {
            JsonNode timelines = data.get("timelines");
            if (timelines != null) {
                JsonNode intervalsArray = timelines.get(0).get("intervals"); // Assuming first timeline is hourly
                if (intervalsArray != null) {
                    int count = 0;
                    for (JsonNode interval : intervalsArray) {
                        if (count >= hours) break; // Limit to requested hours
                        
                        WeatherForecast forecast = new WeatherForecast();
                        forecast.setLatitude(lat);
                        forecast.setLongitude(lon);
                        forecast.setLocationKey(String.format("%.2f,%.2f", lat, lon));
                        
                        // Parse time
                        if (interval.has("time")) {
                            String timeString = interval.get("time").asText();
                            try {
                                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timeString.replace("Z", ""));
                                forecast.setForecastDate(dateTime);
                            } catch (Exception e) {
                                log.warn("Could not parse time: {}", timeString);
                                forecast.setForecastDate(java.time.LocalDateTime.now().plusHours(count));
                            }
                        }
                        
                        // Values
                        JsonNode values = interval.get("values");
                        if (values != null) {
                            if (values.has("temperature")) {
                                forecast.setTemperature(values.get("temperature").asDouble());
                            }
                            if (values.has("temperatureApparent")) {
                                forecast.setFeelsLike(values.get("temperatureApparent").asDouble());
                            }
                            if (values.has("humidity")) {
                                forecast.setHumidity(values.get("humidity").asDouble());
                            }
                            if (values.has("pressureSurfaceLevel")) {
                                forecast.setPressure(values.get("pressureSurfaceLevel").asDouble());
                            }
                            if (values.has("windSpeed")) {
                                forecast.setWindSpeed(values.get("windSpeed").asDouble());
                            }
                            if (values.has("windDirection")) {
                                forecast.setWindDirection(values.get("windDirection").asDouble());
                            }
                            if (values.has("precipitationIntensity")) {
                                forecast.setPrecipitation(values.get("precipitationIntensity").asDouble());
                            }
                            if (values.has("cloudCover")) {
                                forecast.setCloudCoverage(values.get("cloudCover").asDouble());
                            }
                            if (values.has("visibility")) {
                                forecast.setVisibility(values.get("visibility").asDouble() / 1000.0); // Convert m to km
                            }
                            if (values.has("uvIndex")) {
                                forecast.setUvIndex(values.get("uvIndex").asDouble());
                            }
                        }
                        
                        forecast.setForecastType(WeatherForecast.ForecastType.HOURLY);
                        forecast.setDataSource("Tomorrow.io");
                        forecasts.add(forecast);
                        count++;
                    }
                }
            }
        }
        
        return forecasts;
    }

    private List<WeatherForecast> mapOpenWeatherHourlyForecastResponse(JsonNode json, double lat, double lon, int hours) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        // Create a single forecast entry from current weather for compatibility
        WeatherForecast forecast = new WeatherForecast();
        forecast.setLatitude(lat);
        forecast.setLongitude(lon);
        forecast.setLocationKey(String.format("%.2f,%.2f", lat, lon));
        forecast.setForecastDate(java.time.LocalDateTime.now());
        forecast.setForecastType(WeatherForecast.ForecastType.HOURLY);
        forecast.setDataSource("OpenWeatherMap");
        
        JsonNode main = json.get("main");
        if (main != null) {
            if (main.has("temp")) {
                forecast.setTemperature(main.get("temp").asDouble());
            }
            if (main.has("feels_like")) {
                forecast.setFeelsLike(main.get("feels_like").asDouble());
            }
            if (main.has("humidity")) {
                forecast.setHumidity(main.get("humidity").asDouble());
            }
            if (main.has("pressure")) {
                forecast.setPressure(main.get("pressure").asDouble());
            }
        }
        
        JsonNode wind = json.get("wind");
        if (wind != null) {
            if (wind.has("speed")) {
                forecast.setWindSpeed(wind.get("speed").asDouble());
            }
            if (wind.has("deg")) {
                forecast.setWindDirection(wind.get("deg").asDouble());
            }
        }
        
        forecasts.add(forecast);
        
        // In a complete implementation, you would generate forecasts for each hour up to 'hours'
        return forecasts;
    }

    // Note: City-based operations should be handled by WeatherService
    // which has GeocodingService dependency properly configured

    public List<WeatherData> getHistoricalWeather(Double lat, Double lon, LocalDateTime start, LocalDateTime end) {
        if (lat == null || lon == null || start == null || end == null) {
            throw new IllegalArgumentException("Latitude, longitude, start and end times cannot be null");
        }
        
        String locationKey = String.format("%.2f,%.2f", lat, lon);
        return weatherDataRepository.findByLocationKeyAndTimestampBetween(locationKey, start, end);
    }

    public List<WeatherForecast> getHourlyForecast(Double lat, Double lon, int hours) {
        return fetchHourlyForecast(lat, lon, hours);

    }

    private List<WeatherForecast> mapForecastResponse(JsonNode json, double lat, double lon) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        
        if (json != null && json.has("daily")) {
            JsonNode daily = json.get("daily");
            JsonNode timeArray = daily.get("time");
            
            for (int i = 0; i < timeArray.size(); i++) {
                WeatherForecast forecast = new WeatherForecast();
                forecast.setLatitude(lat);
                forecast.setLongitude(lon);
                
                // Parse the date string and convert to LocalDateTime
                String dateString = timeArray.get(i).asText();
                java.time.LocalDate date = java.time.LocalDate.parse(dateString);
                forecast.setForecastDate(date.atStartOfDay());

                if (daily.has("temperature_2m_max") && i < daily.get("temperature_2m_max").size()) {
                    forecast.setTempMax(daily.get("temperature_2m_max").get(i).asDouble());
                }
                if (daily.has("temperature_2m_min") && i < daily.get("temperature_2m_min").size()) {
                    forecast.setTempMin(daily.get("temperature_2m_min").get(i).asDouble());
                }
                if (daily.has("precipitation_sum") && i < daily.get("precipitation_sum").size()) {
                    forecast.setPrecipitation(daily.get("precipitation_sum").get(i).asDouble());
                }
                if (daily.has("wind_speed_10m_max") && i < daily.get("wind_speed_10m_max").size()) {
                    forecast.setWindSpeed(daily.get("wind_speed_10m_max").get(i).asDouble());
                }
                if (daily.has("uv_index_max") && i < daily.get("uv_index_max").size()) {
                    forecast.setUvIndex(daily.get("uv_index_max").get(i).asDouble());
                }
                
                forecast.setForecastType(WeatherForecast.ForecastType.DAILY);

                forecasts.add(forecast);
            }
        }
        
        return forecasts;
    }
}