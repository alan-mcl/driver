package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import za.driver.model.DerivedMetrics;
import za.driver.model.Metric;
import za.driver.model.Vehicle;

class MetricScoresTest {

    @Test
    void score_returnsMappedValue() {
        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(88.0);
        metrics.setRunningCostScore(72.5);
        metrics.setOverallScore(80.0);

        assertEquals(88.0, MetricScores.score(metrics, Metric.SAFETY));
        assertEquals(72.5, MetricScores.score(metrics, Metric.RUNNING_COST));
        assertNull(MetricScores.score(metrics, Metric.PRESTIGE));
        metrics.setAwesomenessScore(65.0);
        assertEquals(65.0, MetricScores.score(metrics, Metric.AWESOMENESS));
    }

    @Test
    void displayScore_fallsBackToLiveReliability() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(80.0);

        Double score = MetricScores.displayScore(vehicle, metrics, Metric.RELIABILITY);

        assertEquals(91.0, score);
    }

    @Test
    void score_nullMetrics_returnsNull() {
        assertNull(MetricScores.score(null, Metric.SAFETY));
    }
}
