package za.driver.scoring;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import za.driver.model.DerivedMetrics;
import za.driver.model.Metric;
import za.driver.model.Pricing;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.model.Vehicle;

public class ScoringService {

    private final List<MetricCalculator> calculators;
    private final ReliabilityCalculator reliabilityCalculator;

    public ScoringService() {
        ReliabilityCalculator reliability = new ReliabilityCalculator();
        this.reliabilityCalculator = reliability;
        this.calculators = List.of(
                new SafetyCalculator(),
                new RunningCostCalculator(),
                reliability,
                new ComfortCalculator(),
                new PerformanceCalculator(),
                new DailyDriverCalculator(),
                new TechnologyCalculator(),
                new PrestigeCalculator());
    }

    ScoringService(List<MetricCalculator> calculators) {
        this.calculators = List.copyOf(calculators);
        this.reliabilityCalculator = calculators.stream()
                .filter(ReliabilityCalculator.class::isInstance)
                .map(ReliabilityCalculator.class::cast)
                .findFirst()
                .orElse(new ReliabilityCalculator());
    }

    ScoringService(List<MetricCalculator> calculators, ReliabilityCalculator reliabilityCalculator) {
        this.calculators = List.copyOf(calculators);
        this.reliabilityCalculator = reliabilityCalculator;
    }

    public ReliabilityCalculator reliabilityCalculator() {
        return reliabilityCalculator;
    }

    public DerivedMetrics calculate(Vehicle vehicle, ScoringProfile profile) {
        return calculate(vehicle, profile, ScoringOverrides.none());
    }

    public DerivedMetrics calculate(Vehicle vehicle, ScoringProfile profile, ScoringOverrides overrides) {
        Map<Metric, Double> scores = new EnumMap<>(Metric.class);

        for (MetricCalculator calculator : calculators) {
            Double score = calculator.calculate(vehicle);
            if (score != null) {
                scores.put(calculator.metric(), ScoreUtil.clamp(score));
            }
        }

        if (overrides != null) {
            if (overrides.getReliabilityScore() != null) {
                scores.put(Metric.RELIABILITY, ScoreUtil.clamp(overrides.getReliabilityScore()));
            }
            if (overrides.getPrestigeScore() != null) {
                scores.put(Metric.PRESTIGE, ScoreUtil.clamp(overrides.getPrestigeScore()));
            }
        }

        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(scores.get(Metric.SAFETY));
        metrics.setRunningCostScore(scores.get(Metric.RUNNING_COST));
        metrics.setReliabilityScore(scores.get(Metric.RELIABILITY));
        metrics.setComfortScore(scores.get(Metric.COMFORT));
        metrics.setPerformanceScore(scores.get(Metric.PERFORMANCE));
        metrics.setDailyDriverScore(scores.get(Metric.DAILY_DRIVER));
        metrics.setTechnologyScore(scores.get(Metric.TECHNOLOGY));
        metrics.setPrestigeScore(scores.get(Metric.PRESTIGE));

        Double awesomenessScore = AwesomenessCalculator.calculate(scores);
        if (awesomenessScore != null) {
            awesomenessScore = ScoreUtil.clamp(awesomenessScore);
            scores.put(Metric.AWESOMENESS, awesomenessScore);
        }
        metrics.setAwesomenessScore(awesomenessScore);
        metrics.setReliabilityConfidence(reliabilityCalculator.confidenceScore(vehicle));
        Double overallScore = calculateOverall(scores, profile);
        metrics.setOverallScore(overallScore);
        metrics.setScorePer100k(calculateScorePer100k(vehicle, overallScore));

        return metrics;
    }

    private static Double calculateScorePer100k(Vehicle vehicle, Double overallScore) {
        if (overallScore == null || vehicle == null) {
            return null;
        }
        Pricing pricing = vehicle.getPricing();
        if (pricing == null || pricing.getPriceZar() == null) {
            return null;
        }
        double price = pricing.getPriceZar().doubleValue();
        if (price <= 0.0) {
            return null;
        }
        return overallScore / price * 100_000.0;
    }

    private static Double calculateOverall(Map<Metric, Double> scores, ScoringProfile profile) {
        if (profile == null || profile.getWeights() == null || profile.getWeights().isEmpty()) {
            return null;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (ScoringWeight scoringWeight : profile.getWeights()) {
            if (scoringWeight.getMetric() == null || scoringWeight.getWeight() == null) {
                continue;
            }

            Double score = scores.get(scoringWeight.getMetric());
            if (score == null) {
                continue;
            }

            weightedSum += score * scoringWeight.getWeight();
            totalWeight += scoringWeight.getWeight();
        }

        if (totalWeight == 0.0) {
            return null;
        }

        return ScoreUtil.clamp(weightedSum / totalWeight);
    }
}
