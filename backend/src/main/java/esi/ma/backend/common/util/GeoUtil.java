package esi.ma.backend.common.util;

public class GeoUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public static double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check if a point is within a bounding box
     */
    public static boolean isWithinBounds(
            double lat, double lon,
            double minLat, double maxLat,
            double minLon, double maxLon
    ) {
        return lat >= minLat && lat <= maxLat &&
                lon >= minLon && lon <= maxLon;
    }

    /**
     * Create location key from coordinates
     */
    public static String createLocationKey(double lat, double lon) {
        return String.format("%.2f,%.2f", lat, lon);
    }

    /**
     * Parse location key to coordinates
     */
    public static double[] parseLocationKey(String locationKey) {
        String[] parts = locationKey.split(",");
        return new double[]{
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1])
        };
    }

    /**
     * Validate latitude
     */
    public static boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }

    /**
     * Validate longitude
     */
    public static boolean isValidLongitude(double lon) {
        return lon >= -180.0 && lon <= 180.0;
    }

    /**
     * Calculate bounding box for a point with radius
     */
    public static BoundingBox calculateBoundingBox(
            double lat, double lon, double radiusKm
    ) {
        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        return new BoundingBox(
                lat - latDelta,  // minLat
                lat + latDelta,  // maxLat
                lon - lonDelta,  // minLon
                lon + lonDelta   // maxLon
        );
    }

    public static class BoundingBox {
        public final double minLat;
        public final double maxLat;
        public final double minLon;
        public final double maxLon;

        public BoundingBox(double minLat, double maxLat, double minLon, double maxLon) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }
    }
}