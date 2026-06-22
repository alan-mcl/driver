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
        if (overrides.getReliabilityScore() != null) {
            manual.setReliabilityScore(overrides.getReliabilityScore());
        }
        if (overrides.getPrestigeScore() != null) {
            manual.setPrestigeScore(overrides.getPrestigeScore());
        }
        vehicle.setManualScoreOverrides(hasAnyOverride(manual) ? manual : null);
    }

    public static void setReliabilityOverride(Vehicle vehicle, Double reliabilityScore) {
        if (vehicle == null) {
            return;
        }
        ManualScoreOverrides manual = vehicle.getManualScoreOverrides();
        if (manual == null) {
            manual = new ManualScoreOverrides();
        }
        manual.setReliabilityScore(reliabilityScore);
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
        return manual.getReliabilityScore() != null || manual.getPrestigeScore() != null;
    }
}
