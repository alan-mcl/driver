package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import za.driver.model.Metric;

class TopWeightedMetricsTest {

    @Test
    void topN_returnsHighestWeightsFirst() {
        List<Metric> top5 = TopWeightedMetrics.topN(ScoringTestFixtures.familyFocusedProfile(), 5);

        assertEquals(
                List.of(
                        Metric.AWESOMENESS,
                        Metric.SAFETY,
                        Metric.RUNNING_COST,
                        Metric.RELIABILITY,
                        Metric.PERFORMANCE),
                top5);
    }

    @Test
    void topN_tieBreaksByMetricOrder() {
        List<Metric> top3 = TopWeightedMetrics.topN(ScoringTestFixtures.equalWeightProfile(), 3);

        assertEquals(List.of(Metric.SAFETY, Metric.RUNNING_COST, Metric.RELIABILITY), top3);
    }
}
