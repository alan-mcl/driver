package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import za.driver.model.Metric;
import za.driver.scoring.ScoringTestFixtures;

class PresentationMetricOrderTest {

    @Test
    void weightedMetrics_ordersByWeightDescending() {
        List<Metric> metrics = PresentationMetricOrder.weightedMetrics(ScoringTestFixtures.familyFocusedProfile());

        assertEquals(
                List.of(
                        Metric.AWESOMENESS,
                        Metric.SAFETY,
                        Metric.RELIABILITY,
                        Metric.RUNNING_COST,
                        Metric.PERFORMANCE),
                metrics);
    }
}
