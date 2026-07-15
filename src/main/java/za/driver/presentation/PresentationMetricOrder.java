package za.driver.presentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;

final class PresentationMetricOrder {

    private PresentationMetricOrder() {
    }

    static List<Metric> weightedMetrics(ScoringProfile profile) {
        if (profile == null || profile.getWeights() == null) {
            return List.of();
        }

        List<ScoringWeight> weights = new ArrayList<>();
        for (ScoringWeight scoringWeight : profile.getWeights()) {
            if (scoringWeight.getMetric() != null && scoringWeight.getWeight() != null) {
                weights.add(scoringWeight);
            }
        }

        weights.sort(Comparator
                .comparing(ScoringWeight::getWeight, Comparator.reverseOrder())
                .thenComparing(
                        scoringWeight -> MetricLabels.displayName(scoringWeight.getMetric(), profile),
                        String.CASE_INSENSITIVE_ORDER));

        List<Metric> metrics = new ArrayList<>();
        for (ScoringWeight scoringWeight : weights) {
            metrics.add(scoringWeight.getMetric());
        }
        return List.copyOf(metrics);
    }
}
