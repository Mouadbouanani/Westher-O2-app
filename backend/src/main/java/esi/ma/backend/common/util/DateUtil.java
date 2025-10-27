package esi.ma.backend.common.util;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Format LocalDateTime to ISO string
     */
    public static String toIsoString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(ISO_FORMATTER);
    }

    /**
     * Format LocalDateTime to display string
     */
    public static String toDisplayString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Parse ISO string to LocalDateTime
     */
    public static LocalDateTime fromIsoString(String isoString) {
        if (isoString == null || isoString.isEmpty()) return null;
        return LocalDateTime.parse(isoString, ISO_FORMATTER);
    }

    /**
     * Get current UTC time
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * Check if date is within last N hours
     */
    public static boolean isWithinLastHours(LocalDateTime dateTime, int hours) {
        if (dateTime == null) return false;
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return dateTime.isAfter(threshold);
    }

    /**
     * Calculate hours between two dates
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }
}