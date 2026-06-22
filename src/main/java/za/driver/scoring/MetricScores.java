package za.driver.scoring;

import za.driver.model.DerivedMetrics;
import za.driver.model.Metric;
import za.driver.model.Vehicle;

public final class MetricScores {

    private static final ReliabilityCalculator RELIABILITY_CALCULATOR = new ReliabilityCalculator();

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
            score = RELIABILITY_CALCULATOR.calculate(vehicle);
        }
        return score;
    }
}
