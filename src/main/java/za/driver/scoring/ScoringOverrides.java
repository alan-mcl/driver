package za.driver.scoring;

import za.driver.model.DerivedMetrics;
import za.driver.model.ManualScoreOverrides;
import za.driver.model.Vehicle;

public class ScoringOverrides {

    private Double reliabilityScore;
    private Double prestigeScore;

    public ScoringOverrides() {
    }

    public static ScoringOverrides none() {
        return new ScoringOverrides();
    }

    public static ScoringOverrides of(Double reliabilityScore, Double prestigeScore) {
        ScoringOverrides overrides = new ScoringOverrides();
        overrides.setReliabilityScore(reliabilityScore);
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
        Double reliability = overrides.getReliabilityScore();
        Double prestige = overrides.getPrestigeScore();
        if (reliability == null && prestige == null) {
            return none();
        }
        return of(reliability, prestige);
    }

    /** @deprecated use {@link #fromVehicle(Vehicle)} — derived metrics must not be treated as overrides */
    @Deprecated
    public static ScoringOverrides fromDerivedMetrics(DerivedMetrics metrics) {
        if (metrics == null) {
            return none();
        }
        Double reliability = metrics.getReliabilityScore();
        Double prestige = metrics.getPrestigeScore();
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
        Double reliability = partial.getReliabilityScore() != null
                ? partial.getReliabilityScore()
                : existing.getReliabilityScore();
        Double prestige = partial.getPrestigeScore() != null
                ? partial.getPrestigeScore()
                : existing.getPrestigeScore();
        if (reliability == null && prestige == null) {
            return none();
        }
        return of(reliability, prestige);
    }

    public Double getReliabilityScore() {
        return reliabilityScore;
    }

    public void setReliabilityScore(Double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }

    public Double getPrestigeScore() {
        return prestigeScore;
    }

    public void setPrestigeScore(Double prestigeScore) {
        this.prestigeScore = prestigeScore;
    }
}
