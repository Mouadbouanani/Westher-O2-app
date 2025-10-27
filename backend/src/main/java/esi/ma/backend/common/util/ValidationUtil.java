package esi.ma.backend.common.util;
import esi.ma.backend.common.util.DateUtil;
import esi.ma.backend.common.exception.InvalidRequestException;

public class ValidationUtil {

    /**
     * Validate latitude and longitude
     */
    public static void validateCoordinates(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new InvalidRequestException("Latitude and longitude are required");
        }

        if (!GeoUtil.isValidLatitude(lat)) {
            throw new InvalidRequestException(
                    "Invalid latitude: " + lat + ". Must be between -90 and 90");
        }

        if (!GeoUtil.isValidLongitude(lon)) {
            throw new InvalidRequestException(
                    "Invalid longitude: " + lon + ". Must be between -180 and 180");
        }
    }

    /**
     * Validate date range
     */
    public static void validateDateRange(
            java.time.LocalDateTime start,
            java.time.LocalDateTime end,
            int maxDays
    ) {
        if (start == null || end == null) {
            throw new InvalidRequestException("Start and end dates are required");
        }

        if (start.isAfter(end)) {
            throw new InvalidRequestException("Start date must be before end date");
        }

        long days = DateUtil.daysBetween(start, end);
        if (days > maxDays) {
            throw new InvalidRequestException(
                    "Date range cannot exceed " + maxDays + " days");
        }
    }

    /**
     * Validate pagination parameters
     */
    public static void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new InvalidRequestException("Page size must be between 1 and 100");
        }
    }

    /**
     * Validate string not empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidRequestException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate numeric range
     */
    public static void validateRange(
            Number value,
            Number min,
            Number max,
            String fieldName
    ) {
        if (value == null) {
            throw new InvalidRequestException(fieldName + " is required");
        }

        double val = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();

        if (val < minVal || val > maxVal) {
            throw new InvalidRequestException(
                    fieldName + " must be between " + min + " and " + max);
        }
    }

    /**
     * Sanitize text input to prevent XSS
     */
    public static String sanitizeText(String input) {
        if (input == null) return null;

        return input
                .replaceAll("<script>", "")
                .replaceAll("</script>", "")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;")
                .trim();
    }
}