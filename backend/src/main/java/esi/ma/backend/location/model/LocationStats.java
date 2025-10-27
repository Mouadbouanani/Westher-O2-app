package esi.ma.backend.location.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;

@Entity
@Table(name = "location_stats", indexes = {
        @Index(name = "idx_location_key", columnList = "location_key"),
        @Index(name = "idx_search_count", columnList = "search_count DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
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

    public Long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Long searchCount) {
        this.searchCount = searchCount;
    }

    public Long getWeatherRequestCount() {
        return weatherRequestCount;
    }

    public void setWeatherRequestCount(Long weatherRequestCount) {
        this.weatherRequestCount = weatherRequestCount;
    }

    public Long getAqiRequestCount() {
        return aqiRequestCount;
    }

    public void setAqiRequestCount(Long aqiRequestCount) {
        this.aqiRequestCount = aqiRequestCount;
    }

    public LocalDateTime getFirstAccessedAt() {
        return firstAccessedAt;
    }

    public void setFirstAccessedAt(LocalDateTime firstAccessedAt) {
        this.firstAccessedAt = firstAccessedAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    @Column(unique = true, nullable = false)
    private String locationKey; // "lat,lon"

    private String cityName;
    private String countryName;
    private String countryCode;
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Long searchCount = 0L;

    @Column(nullable = false)
    private Long weatherRequestCount = 0L;

    @Column(nullable = false)
    private Long aqiRequestCount = 0L;

    @CreationTimestamp
    private LocalDateTime firstAccessedAt;

    @UpdateTimestamp
    private LocalDateTime lastAccessedAt;

    // Increment methods
    public void incrementSearchCount() {
        this.searchCount++;
    }

    public void incrementWeatherRequestCount() {
        this.weatherRequestCount++;
    }

    public void incrementAqiRequestCount() {
        this.aqiRequestCount++;
    }
}