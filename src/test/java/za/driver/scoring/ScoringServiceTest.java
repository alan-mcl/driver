package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.driver.model.DerivedMetrics;
import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.model.Vehicle;

class ScoringServiceTest {

    private ScoringService scoringService;
    private Vehicle fullVehicle;
    private ScoringProfile familyFocusedProfile;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
        fullVehicle = ScoringTestFixtures.fullVehicle();
        familyFocusedProfile = ScoringTestFixtures.familyFocusedProfile();
    }

    @Test
    void calculate_fullVehicle_producesAllMetricsInRange() {
        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile);

        assertScoreInRange(metrics.getSafetyScore());
        assertScoreInRange(metrics.getRunningCostScore());
        assertScoreInRange(metrics.getComfortScore());
        assertScoreInRange(metrics.getPerformanceScore());
        assertScoreInRange(metrics.getDailyDriverScore());
        assertScoreInRange(metrics.getTechnologyScore());
        assertEquals(91.0, metrics.getReliabilityHeuristic());
        assertEquals(91.0, metrics.getReliabilityScore());
        assertEquals(98, metrics.getReliabilityConfidence());
        assertNull(metrics.getPrestigeScore());
        assertNotNull(metrics.getAwesomenessScore());
        assertNotNull(metrics.getOverallScore());
    }

    @Test
    void calculate_withManualEstimate_blendsWithHeuristic() {
        ScoringOverrides overrides = ScoringOverrides.of(88.0, 72.0);

        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile, overrides);

        assertEquals(91.0, metrics.getReliabilityHeuristic());
        assertEquals(90.0, metrics.getReliabilityScore());
        assertEquals(72.0, metrics.getPrestigeScore());
    }

    @Test
    void calculate_withManualEstimateOnly_usesEstimate() {
        Vehicle partialVehicle = ScoringTestFixtures.partialVehicle();
        partialVehicle.setMake("Toyota");

        DerivedMetrics metrics = scoringService.calculate(
                partialVehicle,
                familyFocusedProfile,
                ScoringOverrides.of(80.0, null));

        assertNull(metrics.getReliabilityHeuristic());
        assertEquals(80.0, metrics.getReliabilityScore());
    }

    @Test
    void calculate_withHeuristicOnly_usesHeuristic() {
        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile);

        assertEquals(91.0, metrics.getReliabilityHeuristic());
        assertEquals(91.0, metrics.getReliabilityScore());
    }

    @Test
    void calculate_awesomeness_composesComponentScores() {
        ScoringOverrides overrides = ScoringOverrides.of(null, 80.0);

        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile, overrides);

        double expected = (
                80.0 * 55.0
                + metrics.getComfortScore() * 15.0
                + metrics.getDailyDriverScore() * 15.0
                + metrics.getTechnologyScore() * 15.0
        ) / 100.0;
        assertEquals(expected, metrics.getAwesomenessScore(), 0.01);
    }

    @Test
    void calculate_awesomeness_renormalizesWhenComponentsMissing() {
        Vehicle partialVehicle = ScoringTestFixtures.partialVehicle();
        ScoringOverrides overrides = ScoringOverrides.of(null, 60.0);

        DerivedMetrics metrics = scoringService.calculate(partialVehicle, familyFocusedProfile, overrides);

        assertEquals(60.0, metrics.getAwesomenessScore());
    }

    @Test
    void calculate_appliesProfileWeights_correctOverall() {
        ScoringOverrides overrides = ScoringOverrides.of(90.0, 50.0);

        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile, overrides);

        double expected = (
                metrics.getSafetyScore() * 25
                + metrics.getRunningCostScore() * 15
                + metrics.getReliabilityScore() * 15
                + metrics.getPerformanceScore() * 5
                + metrics.getAwesomenessScore() * 40
        ) / 100.0;

        assertEquals(expected, metrics.getOverallScore(), 0.01);
    }

    @Test
    void calculate_partialVehicle_excludesNullMetricsFromOverall() {
        Vehicle partialVehicle = ScoringTestFixtures.partialVehicle();

        DerivedMetrics metrics = scoringService.calculate(partialVehicle, familyFocusedProfile);

        assertNull(metrics.getSafetyScore());
        assertNull(metrics.getRunningCostScore());
        assertNull(metrics.getOverallScore());
    }

    @Test
    void calculate_emptyProfile_overallIsNull() {
        ScoringProfile emptyProfile = ScoringTestFixtures.emptyProfile();

        DerivedMetrics metrics = scoringService.calculate(fullVehicle, emptyProfile);

        assertNotNull(metrics.getSafetyScore());
        assertNull(metrics.getOverallScore());
    }

    @Test
    void calculate_fullVehicle_computesScorePer100k() {
        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile);

        assertNotNull(metrics.getOverallScore());
        assertNotNull(metrics.getScorePer100k());
        assertEquals(metrics.getOverallScore() / 350_000.0 * 100_000.0, metrics.getScorePer100k(), 0.01);
    }

    @Test
    void calculate_missingPrice_scorePer100kIsNull() {
        fullVehicle.setPricing(null);

        DerivedMetrics metrics = scoringService.calculate(fullVehicle, familyFocusedProfile);

        assertNotNull(metrics.getOverallScore());
        assertNull(metrics.getScorePer100k());
    }

    @Test
    void calculate_partialVehicle_scorePer100kIsNull() {
        DerivedMetrics metrics = scoringService.calculate(ScoringTestFixtures.partialVehicle(), familyFocusedProfile);

        assertNull(metrics.getOverallScore());
        assertNull(metrics.getScorePer100k());
    }

    @Test
    void calculate_awesomeness_usesProfileAggregateComponents() {
        ScoringProfile customProfile = ScoringTestFixtures.familyFocusedProfile();
        List<ScoringWeight> components = new ArrayList<>();
        components.add(weight(Metric.PRESTIGE, 25.0));
        components.add(weight(Metric.COMFORT, 25.0));
        components.add(weight(Metric.DAILY_DRIVER, 25.0));
        components.add(weight(Metric.TECHNOLOGY, 25.0));
        customProfile.setAggregateComponents(components);

        ScoringOverrides overrides = ScoringOverrides.of(null, 80.0);
        DerivedMetrics metrics = scoringService.calculate(fullVehicle, customProfile, overrides);

        double expected = (
                80.0 * 25.0
                + metrics.getComfortScore() * 25.0
                + metrics.getDailyDriverScore() * 25.0
                + metrics.getTechnologyScore() * 25.0
        ) / 100.0;
        assertEquals(expected, metrics.getAwesomenessScore(), 0.01);
    }

    @Test
    void calculate_awesomeness_fallsBackToLegacyConstantsWithoutComponents() {
        ScoringProfile profileWithoutComponents = ScoringTestFixtures.familyFocusedProfile();
        profileWithoutComponents.setAggregateComponents(null);

        ScoringOverrides overrides = ScoringOverrides.of(null, 80.0);
        DerivedMetrics metrics = scoringService.calculate(fullVehicle, profileWithoutComponents, overrides);

        DerivedMetrics withDefaults = scoringService.calculate(fullVehicle, familyFocusedProfile, overrides);
        assertEquals(withDefaults.getAwesomenessScore(), metrics.getAwesomenessScore());
    }

    @Test
    void calculate_renormalizesWeightsWhenSomeMetricsMissing() {
        ScoringProfile safetyOnlyProfile = new ScoringProfile();
        safetyOnlyProfile.setName("Safety Only");
        safetyOnlyProfile.getWeights().add(weight(Metric.SAFETY, 60.0));
        safetyOnlyProfile.getWeights().add(weight(Metric.RELIABILITY, 40.0));

        DerivedMetrics metrics = scoringService.calculate(fullVehicle, safetyOnlyProfile);

        assertNotNull(metrics.getSafetyScore());
        assertEquals(91.0, metrics.getReliabilityScore());
        double expectedOverall = (
                metrics.getSafetyScore() * 60.0
                + metrics.getReliabilityScore() * 40.0
        ) / 100.0;
        assertEquals(expectedOverall, metrics.getOverallScore(), 0.01);
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }

    private static void assertScoreInRange(Double score) {
        assertNotNull(score);
        assertTrue(score >= 0.0 && score <= 100.0, "Score out of range: " + score);
    }
}
