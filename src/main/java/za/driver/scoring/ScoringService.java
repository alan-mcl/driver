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
import za.driver.service.BrandReliabilityConfigService;

public class ScoringService {

    private final List<MetricCalculator> calculators;
    private final BrandReliabilityConfigService brandReliabilityConfigService;
    private final PowertrainReliabilityScorer powertrainScorer;

    public ScoringService() {
        this((BrandReliabilityConfigService) null);
    }

    public ScoringService(BrandReliabilityConfigService brandReliabilityConfigService) {
        this.brandReliabilityConfigService = brandReliabilityConfigService;
        this.powertrainScorer = new PowertrainReliabilityScorer();
        this.calculators = List.of(
                new SafetyCalculator(),
                new RunningCostCalculator(),
                new ComfortCalculator(),
                new PerformanceCalculator(),
                new DailyDriverCalculator(),
                new TechnologyCalculator(),
                new PrestigeCalculator());
    }

    ScoringService(List<MetricCalculator> calculators) {
        this.calculators = List.copyOf(calculators);
        this.brandReliabilityConfigService = null;
        this.powertrainScorer = new PowertrainReliabilityScorer();
    }

    ScoringService(List<MetricCalculator> calculators, ReliabilityCalculator reliabilityCalculator) {
        this.calculators = List.copyOf(calculators);
        this.brandReliabilityConfigService = null;
        this.powertrainScorer = new PowertrainReliabilityScorer();
    }

    public ReliabilityCalculator reliabilityCalculator() {
        return currentReliabilityCalculator();
    }

    private ReliabilityCalculator currentReliabilityCalculator() {
        if (brandReliabilityConfigService != null) {
            return new ReliabilityCalculator(brandReliabilityConfigService.getMergedLookup(), powertrainScorer);
        }
        return new ReliabilityCalculator(BrandReliabilityLookup.getDefault(), powertrainScorer);
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
            if (overrides.getPrestigeScore() != null) {
                scores.put(Metric.PRESTIGE, ScoreUtil.clamp(overrides.getPrestigeScore()));
            }
        }

        Double reliabilityHeuristic = currentReliabilityCalculator().calculate(vehicle);
        Double reliabilityManualEstimate = overrides != null ? overrides.getReliabilityManualEstimate() : null;
        Double reliabilityScore = ReliabilityScoreBlender.blend(reliabilityHeuristic, reliabilityManualEstimate);
        if (reliabilityScore != null) {
            scores.put(Metric.RELIABILITY, reliabilityScore);
        }

        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(scores.get(Metric.SAFETY));
        metrics.setRunningCostScore(scores.get(Metric.RUNNING_COST));
        metrics.setReliabilityHeuristic(reliabilityHeuristic);
        metrics.setReliabilityScore(reliabilityScore);
        metrics.setComfortScore(scores.get(Metric.COMFORT));
        metrics.setPerformanceScore(scores.get(Metric.PERFORMANCE));
        metrics.setDailyDriverScore(scores.get(Metric.DAILY_DRIVER));
        metrics.setTechnologyScore(scores.get(Metric.TECHNOLOGY));
        metrics.setPrestigeScore(scores.get(Metric.PRESTIGE));

        List<ScoringWeight> aggregateComponents = profile != null ? profile.getAggregateComponents() : null;
        Double awesomenessScore = AwesomenessCalculator.calculate(scores, aggregateComponents);
        if (awesomenessScore != null) {
            awesomenessScore = ScoreUtil.clamp(awesomenessScore);
            scores.put(Metric.AWESOMENESS, awesomenessScore);
        }
        metrics.setAwesomenessScore(awesomenessScore);
        metrics.setReliabilityConfidence(currentReliabilityCalculator().confidenceScore(vehicle));
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
        if (pricing == null || pricing.effectivePrice() == null) {
            return null;
        }
        double price = pricing.effectivePrice().doubleValue();
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
