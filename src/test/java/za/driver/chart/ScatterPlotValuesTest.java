package za.driver.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.driver.model.DerivedMetrics;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.scoring.ScoringTestFixtures;

class ScatterPlotValuesTest {

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = ScoringTestFixtures.fullVehicle();
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("450000"));
        vehicle.setPricing(pricing);

        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setOverallScore(78.0);
        metrics.setSafetyScore(85.0);
        metrics.setRunningCostScore(70.0);
        metrics.setReliabilityScore(90.0);
        metrics.setPerformanceScore(65.0);
        metrics.setAwesomenessScore(75.0);
        metrics.setScorePer100k(17.3);
        vehicle.setDerivedMetrics(metrics);
    }

    @Test
    void value_price_returnsZarAmount() {
        assertEquals(450000.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.PRICE).orElseThrow());
    }

    @Test
    void value_overallScore_returnsMetric() {
        assertEquals(78.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.OVERALL_SCORE).orElseThrow());
    }

    @Test
    void value_scorePer100k_returnsMetric() {
        assertEquals(17.3, ScatterPlotValues.value(vehicle, ScatterPlotAxis.SCORE_PER_100K).orElseThrow());
    }

    @Test
    void value_metricAxes_returnStoredScores() {
        assertEquals(85.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.SAFETY).orElseThrow());
        assertEquals(70.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.RUNNING_COST).orElseThrow());
        assertEquals(90.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.RELIABILITY).orElseThrow());
        assertEquals(65.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.PERFORMANCE).orElseThrow());
        assertEquals(75.0, ScatterPlotValues.value(vehicle, ScatterPlotAxis.AWESOMENESS).orElseThrow());
    }

    @Test
    void value_reliability_computesWhenStoredScoreMissing() {
        vehicle.getDerivedMetrics().setReliabilityScore(null);
        assertTrue(ScatterPlotValues.value(vehicle, ScatterPlotAxis.RELIABILITY).isPresent());
    }

    @Test
    void value_missingPricing_returnsEmpty() {
        vehicle.setPricing(null);
        assertFalse(ScatterPlotValues.value(vehicle, ScatterPlotAxis.PRICE).isPresent());
    }

    @Test
    void value_missingDerivedMetrics_returnsEmptyForScores() {
        vehicle.setDerivedMetrics(null);
        assertFalse(ScatterPlotValues.value(vehicle, ScatterPlotAxis.OVERALL_SCORE).isPresent());
        assertFalse(ScatterPlotValues.value(vehicle, ScatterPlotAxis.SCORE_PER_100K).isPresent());
    }

    @Test
    void value_nullVehicle_returnsEmpty() {
        assertFalse(ScatterPlotValues.value(null, ScatterPlotAxis.PRICE).isPresent());
    }
}
