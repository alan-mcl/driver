package za.driver.scoring;

import static za.driver.scoring.ScoringConstants.RELIABILITY_BRAND_WEIGHT;
import static za.driver.scoring.ScoringConstants.RELIABILITY_PARTS_SUPPORT_WEIGHT;
import static za.driver.scoring.ScoringConstants.RELIABILITY_POWERTRAIN_WEIGHT;

import za.driver.model.Metric;
import za.driver.model.Ownership;
import za.driver.model.Vehicle;

public class ReliabilityCalculator implements MetricCalculator {

    private final BrandReliabilityLookup brandLookup;
    private final PowertrainReliabilityScorer powertrainScorer;

    public ReliabilityCalculator() {
        this(BrandReliabilityLookup.getDefault(), new PowertrainReliabilityScorer());
    }

    ReliabilityCalculator(BrandReliabilityLookup brandLookup, PowertrainReliabilityScorer powertrainScorer) {
        this.brandLookup = brandLookup;
        this.powertrainScorer = powertrainScorer;
    }

    @Override
    public Metric metric() {
        return Metric.RELIABILITY;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        return breakdown(vehicle).score();
    }

    public ReliabilityBreakdown breakdown(Vehicle vehicle) {
        if (vehicle == null) {
            return ReliabilityBreakdown.unavailable(null, null, null, null, null);
        }

        Integer brandScore = brandLookup.reliabilityScore(vehicle.getMake());
        String brandName = brandLookup.displayName(vehicle.getMake());
        Ownership ownership = vehicle.getOwnership();
        Integer partsScore = ownership != null ? ownership.getPartsSupportScore() : null;
        PowertrainReliabilityScorer.PowertrainScoreDetails powertrain =
                powertrainScorer.scoreDetails(vehicle.getEngine(), vehicle.getTransmission());

        if (brandScore == null || partsScore == null || powertrain.score() == null) {
            return ReliabilityBreakdown.unavailable(brandScore, brandName, powertrain.score(), partsScore, null);
        }

        double raw = brandScore * RELIABILITY_BRAND_WEIGHT
                + powertrain.score() * RELIABILITY_POWERTRAIN_WEIGHT
                + partsScore * RELIABILITY_PARTS_SUPPORT_WEIGHT;
        double rounded = Math.round(ScoreUtil.clamp(raw));
        return new ReliabilityBreakdown(
                rounded,
                brandScore,
                brandName,
                powertrain.score(),
                powertrain.explanation(),
                partsScore);
    }

    public Integer confidenceScore(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return brandLookup.confidenceScore(vehicle.getMake());
    }

    public record ReliabilityBreakdown(
            Double score,
            Integer brandScore,
            String brandName,
            Double powertrainScore,
            String powertrainExplanation,
            Integer partsSupportScore) {

        static ReliabilityBreakdown unavailable(
                Integer brandScore,
                String brandName,
                Double powertrainScore,
                Integer partsSupportScore,
                String powertrainExplanation) {
            return new ReliabilityBreakdown(
                    null, brandScore, brandName, powertrainScore, powertrainExplanation, partsSupportScore);
        }

        boolean isComplete() {
            return score != null;
        }
    }
}
