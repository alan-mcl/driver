package za.driver.scoring;

import za.driver.model.ManualScoreOverrides;
import za.driver.model.Vehicle;

public final class ManualScoreOverrideUtil {

    private ManualScoreOverrideUtil() {
    }

    public static void applyOverrides(Vehicle vehicle, ScoringOverrides overrides) {
        if (vehicle == null || overrides == null) {
            return;
        }
        ManualScoreOverrides manual = vehicle.getManualScoreOverrides();
        if (manual == null) {
            manual = new ManualScoreOverrides();
        }
        if (overrides.getReliabilityManualEstimate() != null) {
            manual.setReliabilityManualEstimate(overrides.getReliabilityManualEstimate());
        }
        if (overrides.getPrestigeScore() != null) {
            manual.setPrestigeScore(overrides.getPrestigeScore());
        }
        vehicle.setManualScoreOverrides(hasAnyOverride(manual) ? manual : null);
    }

    public static void setReliabilityManualEstimate(Vehicle vehicle, Double reliabilityManualEstimate) {
        if (vehicle == null) {
            return;
        }
        ManualScoreOverrides manual = vehicle.getManualScoreOverrides();
        if (manual == null) {
            manual = new ManualScoreOverrides();
        }
        manual.setReliabilityManualEstimate(reliabilityManualEstimate);
        vehicle.setManualScoreOverrides(hasAnyOverride(manual) ? manual : null);
    }

    public static void setPrestigeOverride(Vehicle vehicle, Double prestigeScore) {
        if (vehicle == null) {
            return;
        }
        ManualScoreOverrides manual = vehicle.getManualScoreOverrides();
        if (manual == null) {
            manual = new ManualScoreOverrides();
        }
        manual.setPrestigeScore(prestigeScore);
        vehicle.setManualScoreOverrides(hasAnyOverride(manual) ? manual : null);
    }

    private static boolean hasAnyOverride(ManualScoreOverrides manual) {
        return manual.getReliabilityManualEstimate() != null || manual.getPrestigeScore() != null;
    }
}
