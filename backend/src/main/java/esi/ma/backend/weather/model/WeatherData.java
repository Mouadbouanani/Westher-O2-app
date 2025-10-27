package esi.ma.backend.weather.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data", indexes = {
        @Index(name = "idx_location_timestamp", columnList = "location_key, timestamp"),
        @Index(name = "idx_coordinates", columnList = "latitude, longitude")
})
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Location info
    private String locationKey;      // Format: "lat,lon" (e.g., "34.05,-118.24")
    private String cityName;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private Integer altitude;        // meters above sea level

    // Weather metrics
    private Double temperature;      // Celsius
    private Double feelsLike;
    private Double tempMin;
    private Double tempMax;
    private Double humidity;         // percentage
    private Double pressure;         // hPa
    private Double windSpeed;        // m/s
    private Double windDirection;    // degrees
    private Double uvIndex;
    private Double visibility;       // km
    private Double cloudCoverage;    // percentage
    private Double precipitation;    // mm
    private Double dewPoint;         // Celsius
    private Integer weatherCode;     // Weather condition code

    // Conditions
    private String weatherMain;      // Clear, Clouds, Rain, etc.
    private String weatherDescription;
    private String iconCode;

    // Metadata
    @CreationTimestamp
    private LocalDateTime timestamp;
    private String dataSource;       // OpenMeteo, OpenWeatherMap, etc.
    private LocalDateTime expiresAt; // Cache expiration

    public void setWeatherDescription(String description) {
        this.weatherDescription = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(Double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public Double getTempMin() {
        return tempMin;
    }

    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    public Double getTempMax() {
        return tempMax;
    }

    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Double windDirection) {
        this.windDirection = windDirection;
    }

    public Double getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(Double uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Double getVisibility() {
        return visibility;
    }

    public void setVisibility(Double visibility) {
        this.visibility = visibility;
    }

    public Double getCloudCoverage() {
        return cloudCoverage;
    }

    public void setCloudCoverage(Double cloudCoverage) {
        this.cloudCoverage = cloudCoverage;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public Double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(Double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDataSource() {
        return dataSource;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setTemperature(double temp) {
        this.temperature = temp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getCityName() {
        return cityName;
    }

    public Double getPressure() {
        return pressure;

    }

    public Double getHumidity() {
        return humidity;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setWeatherCode(int weathercode) {
        this.weatherCode = weathercode;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    // Getters and setters...


}