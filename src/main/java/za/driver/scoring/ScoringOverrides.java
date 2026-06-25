package za.driver.scoring;

import za.driver.model.ManualScoreOverrides;
import za.driver.model.Vehicle;

public class ScoringOverrides {

    private Double reliabilityManualEstimate;
    private Double prestigeScore;

    public ScoringOverrides() {
    }

    public static ScoringOverrides none() {
        return new ScoringOverrides();
    }

    public static ScoringOverrides of(Double reliabilityManualEstimate, Double prestigeScore) {
        ScoringOverrides overrides = new ScoringOverrides();
        overrides.setReliabilityManualEstimate(reliabilityManualEstimate);
        overrides.setPrestigeScore(prestigeScore);
        return overrides;
    }

    public static ScoringOverrides fromVehicle(Vehicle vehicle) {
        if (vehicle == null || vehicle.getManualScoreOverrides() == null) {
            return none();
        }
        return fromManualScoreOverrides(vehicle.getManualScoreOverrides());
    }

    public static ScoringOverrides fromManualScoreOverrides(ManualScoreOverrides overrides) {
        if (overrides == null) {
            return none();
        }
        Double reliability = overrides.getReliabilityManualEstimate();
        Double prestige = overrides.getPrestigeScore();
        if (reliability == null && prestige == null) {
            return none();
        }
        return of(reliability, prestige);
    }

    public static ScoringOverrides merge(ScoringOverrides existing, ScoringOverrides partial) {
        if (partial == null) {
            return existing == null ? none() : existing;
        }
        if (existing == null) {
            existing = none();
        }
        Double reliability = partial.getReliabilityManualEstimate() != null
                ? partial.getReliabilityManualEstimate()
                : existing.getReliabilityManualEstimate();
        Double prestige = partial.getPrestigeScore() != null
                ? partial.getPrestigeScore()
                : existing.getPrestigeScore();
        if (reliability == null && prestige == null) {
            return none();
        }
        return of(reliability, prestige);
    }

    public Double getReliabilityManualEstimate() {
        return reliabilityManualEstimate;
    }

    public void setReliabilityManualEstimate(Double reliabilityManualEstimate) {
        this.reliabilityManualEstimate = reliabilityManualEstimate;
    }

    public Double getPrestigeScore() {
        return prestigeScore;
    }

    public void setPrestigeScore(Double prestigeScore) {
        this.prestigeScore = prestigeScore;
    }
}
