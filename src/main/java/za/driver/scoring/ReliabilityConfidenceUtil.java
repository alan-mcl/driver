package za.driver.scoring;

import za.driver.model.DerivedMetrics;
import za.driver.model.Vehicle;

public final class ReliabilityConfidenceUtil {

    private static final BrandReliabilityLookup LOOKUP = BrandReliabilityLookup.getDefault();

    private ReliabilityConfidenceUtil() {
    }

    public static Integer resolve(Vehicle vehicle, DerivedMetrics metrics) {
        if (metrics != null && metrics.getReliabilityConfidence() != null) {
            return metrics.getReliabilityConfidence();
        }
        if (vehicle == null) {
            return null;
        }
        return LOOKUP.confidenceScore(vehicle.getMake());
    }

    public static String format(Vehicle vehicle, DerivedMetrics metrics) {
        Integer confidence = resolve(vehicle, metrics);
        if (confidence == null) {
            return "-";
        }
        ReliabilityConfidenceBand band = ReliabilityConfidenceBand.fromScore(confidence);
        if (band == null) {
            return confidence.toString();
        }
        return confidence + " (" + band.displayLabel() + ")";
    }
}
