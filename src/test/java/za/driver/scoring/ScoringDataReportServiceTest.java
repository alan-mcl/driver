package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.driver.model.DataQuality;
import za.driver.model.DerivedMetrics;
import za.driver.model.Vehicle;

class ScoringDataReportServiceTest {

    private ScoringDataReportService reportService;
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        reportService = new ScoringDataReportService();
        scoringService = new ScoringService();
    }

    @Test
    void generateReport_fullVehicle_listsManualOverridesOnly() {
        Vehicle vehicle = scoredFullVehicle();

        String report = reportService.generateReport(vehicle, ScoringOverrides.none());

        assertTrue(report.contains("Toyota Corolla 1.8 XS"));
        assertTrue(report.contains("Safety (score:"));
        assertTrue(report.contains("No missing scoring inputs."));
        assertTrue(report.contains("Reliability"));
        assertTrue(report.contains("Brand (Toyota)"));
        assertTrue(report.contains("Confidence: 98 (High)"));
        assertTrue(report.contains("Prestige"));
        assertTrue(report.contains("Awesomeness"));
        assertTrue(report.contains("Overall score:"));
        assertFalse(report.contains("safety.blindSpotMonitoring"));
    }

    @Test
    void generateReport_partialVehicle_listsManyMissingFields() {
        Vehicle vehicle = ScoringTestFixtures.partialVehicle();
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, ScoringTestFixtures.familyFocusedProfile()));

        String report = reportService.generateReport(vehicle, ScoringOverrides.none());

        assertTrue(report.contains("Safety (score: not available)"));
        assertTrue(report.contains("safety"));
        assertTrue(report.contains("economy.fuelConsumptionCombined"));
        assertTrue(report.contains("engine.powerKw"));
        assertTrue(report.contains("Overall score: not available"));
    }

    @Test
    void generateReport_staleDerivedMetrics_usesLiveReliabilityScore() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setSafetyScore(80.0);
        vehicle.setDerivedMetrics(metrics);

        String report = reportService.generateReport(vehicle, ScoringOverrides.none());

        assertTrue(report.contains("Reliability (score: 91)"));
        assertTrue(report.contains("Brand (Toyota)"));
    }

    @Test
    void generateReport_withOverrides_notesReliabilityAndPrestigeSet() {
        Vehicle vehicle = scoredFullVehicle();
        ScoringOverrides overrides = ScoringOverrides.of(90.0, 50.0);

        String report = reportService.generateReport(vehicle, overrides);

        assertTrue(report.contains("Reliability (score: 90)"));
        assertTrue(report.contains("Manual override active."));
        assertTrue(report.contains("Prestige (score: 50.0)"));
        assertTrue(report.contains("Manual override set."));
    }

    @Test
    void generateReport_includesMissingDataQualityHint() {
        Vehicle vehicle = scoredFullVehicle();
        vehicle.setDataQuality(Map.of("features.digitalCluster", DataQuality.MISSING));
        vehicle.getFeatures().setDigitalCluster(null);

        String report = reportService.generateReport(vehicle, ScoringOverrides.none());

        assertTrue(report.contains("features.digitalCluster"));
        assertTrue(report.contains("flagged MISSING at import"));
    }

    @Test
    void generateReport_nullVehicle_returnsPlaceholder() {
        assertNotNull(reportService.generateReport(null, ScoringOverrides.none()));
    }

    private Vehicle scoredFullVehicle() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        DerivedMetrics metrics = scoringService.calculate(vehicle, ScoringTestFixtures.familyFocusedProfile());
        vehicle.setDerivedMetrics(metrics);
        return vehicle;
    }
}
