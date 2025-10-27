package esi.ma.backend.airquality.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "air_quality", indexes = {
        @Index(name = "idx_aqi_location_timestamp", columnList = "location_key, timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirQuality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Location
    private String locationKey;
    private String cityName;
    private Double latitude;
    private Double longitude;

    // AQI
    private Integer aqi; // 0-500
    private String aqiCategory; // Good, Moderate, Unhealthy, etc.
    private String dominantPollutant; // PM2.5, PM10, O3, etc.

    // Individual pollutants (μg/m³)
    private Double pm25;  // Particulate Matter 2.5
    private Double pm10;  // Particulate Matter 10
    private Double no2;   // Nitrogen Dioxide
    private Double so2;   // Sulfur Dioxide
    private Double co;    // Carbon Monoxide (mg/m³)
    private Double o3;    // Ozone

    // Individual AQI for each pollutant
    private Integer pm25Aqi;
    private Integer pm10Aqi;
    private Integer no2Aqi;
    private Integer so2Aqi;
    private Integer coAqi;
    private Integer o3Aqi;

    // Health recommendations
    @Column(columnDefinition = "TEXT")
    private String healthRecommendation;

    @Column(columnDefinition = "TEXT")
    private String sensitiveGroupsAdvice;

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

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public String getAqiCategory() {
        return aqiCategory;
    }

    public void setAqiCategory(String aqiCategory) {
        this.aqiCategory = aqiCategory;
    }

    public String getDominantPollutant() {
        return dominantPollutant;
    }

    public void setDominantPollutant(String dominantPollutant) {
        this.dominantPollutant = dominantPollutant;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public Double getO3() {
        return o3;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public Integer getPm25Aqi() {
        return pm25Aqi;
    }

    public void setPm25Aqi(Integer pm25Aqi) {
        this.pm25Aqi = pm25Aqi;
    }

    public Integer getPm10Aqi() {
        return pm10Aqi;
    }

    public void setPm10Aqi(Integer pm10Aqi) {
        this.pm10Aqi = pm10Aqi;
    }

    public Integer getNo2Aqi() {
        return no2Aqi;
    }

    public void setNo2Aqi(Integer no2Aqi) {
        this.no2Aqi = no2Aqi;
    }

    public Integer getSo2Aqi() {
        return so2Aqi;
    }

    public void setSo2Aqi(Integer so2Aqi) {
        this.so2Aqi = so2Aqi;
    }

    public Integer getCoAqi() {
        return coAqi;
    }

    public void setCoAqi(Integer coAqi) {
        this.coAqi = coAqi;
    }

    public Integer getO3Aqi() {
        return o3Aqi;
    }

    public void setO3Aqi(Integer o3Aqi) {
        this.o3Aqi = o3Aqi;
    }

    public String getHealthRecommendation() {
        return healthRecommendation;
    }

    public void setHealthRecommendation(String healthRecommendation) {
        this.healthRecommendation = healthRecommendation;
    }

    public String getSensitiveGroupsAdvice() {
        return sensitiveGroupsAdvice;
    }

    public void setSensitiveGroupsAdvice(String sensitiveGroupsAdvice) {
        this.sensitiveGroupsAdvice = sensitiveGroupsAdvice;
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

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    @CreationTimestamp
    private LocalDateTime timestamp;

    private String dataSource; // OpenAQ, WAQI, etc.

    // Getters and setters...
}