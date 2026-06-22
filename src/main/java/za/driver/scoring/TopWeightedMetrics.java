package za.driver.scoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;

public final class TopWeightedMetrics {

    private static final Metric[] TIE_BREAK_ORDER = {
            Metric.SAFETY,
            Metric.RUNNING_COST,
            Metric.RELIABILITY,
            Metric.PERFORMANCE,
            Metric.AWESOMENESS
    };

    private TopWeightedMetrics() {
    }

    public static List<Metric> topN(ScoringProfile profile, int n) {
        if (profile == null || profile.getWeights() == null || n <= 0) {
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
                .thenComparing(scoringWeight -> tieBreakIndex(scoringWeight.getMetric())));

        List<Metric> result = new ArrayList<>();
        for (ScoringWeight scoringWeight : weights) {
            if (result.size() >= n) {
                break;
            }
            result.add(scoringWeight.getMetric());
        }
        return List.copyOf(result);
    }

    private static int tieBreakIndex(Metric metric) {
        for (int i = 0; i < TIE_BREAK_ORDER.length; i++) {
            if (TIE_BREAK_ORDER[i] == metric) {
                return i;
            }
        }
        return TIE_BREAK_ORDER.length;
    }
}
