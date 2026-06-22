package za.driver.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import za.driver.model.DerivedMetrics;
import za.driver.model.ManualScoreOverrides;
import za.driver.scoring.ScoringOverrides;
import za.driver.scoring.ScoringTestFixtures;

class VehicleSpreadsheetMapperTest {

    private static final UUID VEHICLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    void toRowValues_roundTripsFullVehicle() {
        var vehicle = ScoringTestFixtures.fullVehicle();
        List<String> row = VehicleSpreadsheetMapper.toRowValues(vehicle);
        assertEquals(VehicleSpreadsheetSchema.columns().size(), row.size());

        var parsed = VehicleSpreadsheetMapper.fromRowValues(row, true);
        assertEquals(vehicle.getId(), parsed.getId());
        assertEquals(vehicle.getMake(), parsed.getMake());
        assertEquals(vehicle.getEngine().getPowerKw(), parsed.getEngine().getPowerKw());
        assertEquals(vehicle.getPricing().getPriceZar(), parsed.getPricing().getPriceZar());
        assertEquals(vehicle.getFeatures().getClimateControlType(), parsed.getFeatures().getClimateControlType());
    }

    @Test
    void fromRowMap_partialRow_setsOnlyProvidedFields() {
        Map<String, String> row = Map.of(
                "id", VEHICLE_ID.toString(),
                "pricing.priceZar", "399000");

        var parsed = VehicleSpreadsheetMapper.fromRowMap(row, false);
        assertEquals(VEHICLE_ID, parsed.getId());
        assertEquals(new BigDecimal("399000"), parsed.getPricing().getPriceZar());
        assertNull(parsed.getMake());
        assertNull(parsed.getStatus());
    }

    @Test
    void headers_matchSchemaColumnCount() {
        assertEquals(VehicleSpreadsheetSchema.columns().size(), VehicleSpreadsheetSchema.headers().size());
    }

    @Test
    void headers_placePricingAfterDerivative() {
        List<String> headers = VehicleSpreadsheetSchema.headers();
        assertEquals(List.of("id", "make", "model", "derivative", "pricing.priceZar", "pricing.priceDate"),
                headers.subList(0, 6));
    }

    @Test
    void toRowValues_exportsDerivedMetricScores() {
        var vehicle = ScoringTestFixtures.fullVehicle();
        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setReliabilityScore(91.0);
        metrics.setPrestigeScore(72.0);
        vehicle.setDerivedMetrics(metrics);

        Map<String, String> row = VehicleSpreadsheetMapper.toRowMap(vehicle);
        assertEquals("91.0", row.get("derivedMetrics.reliabilityScore"));
        assertEquals("72.0", row.get("derivedMetrics.prestigeScore"));
    }

    @Test
    void toRowValues_roundTripsNotes() {
        var vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.setNotes("Test drive scheduled for Saturday.");

        Map<String, String> row = VehicleSpreadsheetMapper.toRowMap(vehicle);
        assertEquals("Test drive scheduled for Saturday.", row.get("notes"));

        var parsed = VehicleSpreadsheetMapper.fromRowMap(
                Map.of("id", VEHICLE_ID.toString(), "notes", "Test drive scheduled for Saturday."),
                false);
        assertEquals("Test drive scheduled for Saturday.", parsed.getNotes());
    }

    @Test
    void toRowValues_exportsManualScoreOverrides() {
        var vehicle = ScoringTestFixtures.fullVehicle();
        DerivedMetrics metrics = new DerivedMetrics();
        metrics.setReliabilityScore(91.0);
        vehicle.setDerivedMetrics(metrics);
        ManualScoreOverrides overrides = new ManualScoreOverrides();
        overrides.setReliabilityScore(88.0);
        overrides.setPrestigeScore(72.0);
        vehicle.setManualScoreOverrides(overrides);

        Map<String, String> row = VehicleSpreadsheetMapper.toRowMap(vehicle);
        assertEquals("91.0", row.get("derivedMetrics.reliabilityScore"));
        assertEquals("72.0", row.get("derivedMetrics.prestigeScore"));
    }

    @Test
    void fromRowMap_roundTripsManualScoreOverrides() {
        Map<String, String> row = Map.of(
                "id", VEHICLE_ID.toString(),
                "derivedMetrics.reliabilityScore", "90",
                "derivedMetrics.prestigeScore", "50");

        var parsed = VehicleSpreadsheetMapper.fromRowMap(row, false);
        assertEquals(90.0, parsed.getManualScoreOverrides().getReliabilityScore());
        assertEquals(50.0, parsed.getManualScoreOverrides().getPrestigeScore());
    }

    @Test
    void scoringOverridesFromRow_includesOnlyNonBlankCells() {
        ScoringOverrides both = VehicleSpreadsheetMapper.scoringOverridesFromRow(Map.of(
                "derivedMetrics.reliabilityScore", "88",
                "derivedMetrics.prestigeScore", "72"));
        assertEquals(88.0, both.getReliabilityScore());
        assertEquals(72.0, both.getPrestigeScore());

        ScoringOverrides reliabilityOnly = VehicleSpreadsheetMapper.scoringOverridesFromRow(Map.of(
                "derivedMetrics.reliabilityScore", "88"));
        assertEquals(88.0, reliabilityOnly.getReliabilityScore());
        assertNull(reliabilityOnly.getPrestigeScore());

        ScoringOverrides none = VehicleSpreadsheetMapper.scoringOverridesFromRow(Map.of());
        assertNull(none.getReliabilityScore());
        assertNull(none.getPrestigeScore());
    }

    @Test
    void scoringOverrides_merge_prefersPartialValues() {
        ScoringOverrides existing = ScoringOverrides.of(80.0, 70.0);
        ScoringOverrides partial = ScoringOverrides.of(90.0, null);

        ScoringOverrides merged = ScoringOverrides.merge(existing, partial);
        assertEquals(90.0, merged.getReliabilityScore());
        assertEquals(70.0, merged.getPrestigeScore());
    }
}
