package za.driver.scoring;

import za.driver.model.DerivedMetrics;
import za.driver.model.ManualScoreOverrides;
import za.driver.model.Metric;
import za.driver.model.Vehicle;
import za.driver.service.BrandReliabilityConfigService;

public final class MetricScores {

    private static final ReliabilityCalculator DEFAULT_RELIABILITY_CALCULATOR = new ReliabilityCalculator();

    private MetricScores() {
    }

    public static Double score(DerivedMetrics metrics, Metric metric) {
        if (metrics == null || metric == null) {
            return null;
        }
        return switch (metric) {
            case SAFETY -> metrics.getSafetyScore();
            case RUNNING_COST -> metrics.getRunningCostScore();
            case RELIABILITY -> metrics.getReliabilityScore();
            case COMFORT -> metrics.getComfortScore();
            case PERFORMANCE -> metrics.getPerformanceScore();
            case DAILY_DRIVER -> metrics.getDailyDriverScore();
            case TECHNOLOGY -> metrics.getTechnologyScore();
            case PRESTIGE -> metrics.getPrestigeScore();
            case AWESOMENESS -> metrics.getAwesomenessScore();
        };
    }

    public static Double displayScore(Vehicle vehicle, DerivedMetrics metrics, Metric metric) {
        Double score = score(metrics, metric);
        if (score == null && metric == Metric.RELIABILITY && vehicle != null) {
            score = liveReliabilityScore(vehicle, null);
        }
        return score;
    }

    public static Double liveReliabilityScore(Vehicle vehicle, BrandReliabilityConfigService brandConfigService) {
        ReliabilityCalculator calculator = brandConfigService == null
                ? DEFAULT_RELIABILITY_CALCULATOR
                : new ReliabilityCalculator(brandConfigService.getMergedLookup(), new PowertrainReliabilityScorer());
        Double heuristic = calculator.calculate(vehicle);
        ManualScoreOverrides overrides = vehicle.getManualScoreOverrides();
        Double manualEstimate = overrides == null ? null : overrides.getReliabilityManualEstimate();
        return ReliabilityScoreBlender.blend(heuristic, manualEstimate);
    }

    public static Double liveReliabilityHeuristic(Vehicle vehicle, BrandReliabilityConfigService brandConfigService) {
        ReliabilityCalculator calculator = brandConfigService == null
                ? DEFAULT_RELIABILITY_CALCULATOR
                : new ReliabilityCalculator(brandConfigService.getMergedLookup(), new PowertrainReliabilityScorer());
        return calculator.calculate(vehicle);
    }
}
