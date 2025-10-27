package esi.ma.backend.crowdsourcing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportCategory {
    AIR_POLLUTION("Air Pollution"),
    SMOKE("Smoke/Fire"),
    INDUSTRIAL_EMISSIONS("Industrial Emissions"),
    VEHICLE_EMISSIONS("Vehicle Emissions"),
    DUST("Dust/Sand Storm"),
    UNUSUAL_WEATHER("Unusual Weather"),
    FLOODING("Flooding"),
    EXTREME_HEAT("Extreme Heat"),
    EXTREME_COLD("Extreme Cold"),
    POLLEN_ALLERGENS("Pollen/Allergens"),
    OTHER("Other");

    private final String displayName;
}
