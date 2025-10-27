package esi.ma.backend.alert.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlertType {
    EXTREME_TEMPERATURE("Extreme Temperature"),
    HIGH_TEMPERATURE("High Temperature"),
    LOW_TEMPERATURE("Low Temperature"),
    EXTREME_WEATHER("Extreme Weather"),
    STORM("Storm Warning"),
    HEAVY_RAIN("Heavy Rain"),
    SNOW("Snow Warning"),
    FOG("Fog Warning"),
    HIGH_WIND("High Wind"),
    POOR_AIR_QUALITY("Poor Air Quality"),
    VERY_UNHEALTHY_AIR("Very Unhealthy Air"),
    HAZARDOUS_AIR("Hazardous Air Quality"),
    LOW_OXYGEN("Low Oxygen Level"),
    DANGEROUS_OXYGEN("Dangerous Oxygen Level"),
    HIGH_UV("High UV Index"),
    EXTREME_HUMIDITY("Extreme Humidity"),
    OTHER("Other Alert");

    private final String displayName;
}