package za.driver.scoring;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;

public final class ProfileScoreMetrics {

    private ProfileScoreMetrics() {
    }

    /**
     * The five profile-level score metrics in display order: four top base metrics by weight,
     * then the aggregate slot ({@link Metric#AWESOMENESS}).
     */
    public static List<Metric> scoreMetrics(ScoringProfile profile) {
        if (profile == null) {
            return List.of();
        }
        List<Metric> top5 = TopWeightedMetrics.topN(profile, 5);
        List<Metric> base = new ArrayList<>();
        boolean hasAggregate = false;
        for (Metric metric : top5) {
            if (metric == Metric.AWESOMENESS) {
                hasAggregate = true;
            } else {
                base.add(metric);
            }
        }
        List<Metric> result = new ArrayList<>(base);
        if (hasAggregate) {
            result.add(Metric.AWESOMENESS);
        }
        return List.copyOf(result);
    }
}
