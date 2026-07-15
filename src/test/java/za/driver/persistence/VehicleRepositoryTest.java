package za.driver.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.DerivedMetrics;
import za.driver.model.ManualScoreOverrides;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;

class VehicleRepositoryTest {

    @TempDir
    Path tempDir;

    private VehicleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new VehicleRepository(tempDir);
    }

    @Test
    void load_legacyPriceZarJson_deserializesAsListPrice() throws IOException {
        String json = """
                {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "make": "Toyota",
                  "model": "Corolla",
                  "pricing": {
                    "priceZar": 350000,
                    "priceDate": "2026-06-17"
                  }
                }
                """;
        Files.createDirectories(tempDir.resolve("vehicles"));
        Files.writeString(tempDir.resolve("vehicles").resolve("550e8400-e29b-41d4-a716-446655440000.json"), json);

        Vehicle loaded = repository.findAll().get(0);

        assertEquals(new BigDecimal("350000"), loaded.getPricing().getListPrice());
        assertNull(loaded.getPricing().getDealerOffer());
        assertEquals(java.time.LocalDate.of(2026, 6, 17), loaded.getPricing().getListPriceDate());
    }

    @Test
    void saveAndLoad_writesNeutralPriceFieldNames() throws IOException {
        Vehicle vehicle = TestFixtures.fullVehicle();
        repository.save(vehicle);

        String json = Files.readString(tempDir.resolve("vehicles").resolve(vehicle.getId() + ".json"));

        assertTrue(json.contains("\"listPrice\""));
        assertFalse(json.contains("listPriceZar"));
    }

    @Test
    void load_legacyListPriceZarJson_deserializesAsListPrice() throws IOException {
        String json = """
                {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "make": "Toyota",
                  "model": "Corolla",
                  "pricing": {
                    "listPriceZar": 350000,
                    "dealerOfferZar": 320000
                  }
                }
                """;
        Files.createDirectories(tempDir.resolve("vehicles"));
        Files.writeString(tempDir.resolve("vehicles").resolve("550e8400-e29b-41d4-a716-446655440000.json"), json);

        Vehicle loaded = repository.findAll().get(0);

        assertEquals(new BigDecimal("350000"), loaded.getPricing().getListPrice());
        assertEquals(new BigDecimal("320000"), loaded.getPricing().getDealerOffer());
    }

    @Test
    void saveAndLoad_fullVehicle_roundTripsWithoutDataLoss() throws IOException {
        Vehicle original = TestFixtures.fullVehicle();

        repository.save(original);
        Vehicle loaded = repository.findById(original.getId()).orElseThrow();

        assertVehiclesEqual(original, loaded);
    }

    @Test
    void saveAndLoad_manualScoreOverridesAndConfidence_roundTrip() throws IOException {
        Vehicle original = TestFixtures.fullVehicle();
        ManualScoreOverrides overrides = new ManualScoreOverrides();
        overrides.setReliabilityManualEstimate(90.0);
        overrides.setPrestigeScore(50.0);
        original.setManualScoreOverrides(overrides);
        DerivedMetrics metrics = original.getDerivedMetrics();
        metrics.setReliabilityConfidence(95);

        repository.save(original);
        Vehicle loaded = repository.findById(original.getId()).orElseThrow();

        assertEquals(90.0, loaded.getManualScoreOverrides().getReliabilityManualEstimate());
        assertEquals(50.0, loaded.getManualScoreOverrides().getPrestigeScore());
        assertEquals(95, loaded.getDerivedMetrics().getReliabilityConfidence());
    }

    @Test
    void saveAndLoad_legacyReliabilityScoreAlias_roundTripsAsManualEstimate() throws IOException {
        java.util.UUID id = java.util.UUID.randomUUID();
        java.nio.file.Files.createDirectories(tempDir.resolve("vehicles"));
        String json = """
                {
                  "id": "%s",
                  "make": "Toyota",
                  "model": "Corolla",
                  "manualScoreOverrides": {
                    "reliabilityScore": 82.0
                  }
                }
                """.formatted(id);
        java.nio.file.Files.writeString(tempDir.resolve("vehicles").resolve(id + ".json"), json);

        Vehicle loaded = repository.findById(id).orElseThrow();
        assertEquals(82.0, loaded.getManualScoreOverrides().getReliabilityManualEstimate());
    }

    @Test
    void saveAndLoad_notes_roundTrip() throws IOException {
        Vehicle original = TestFixtures.fullVehicle();
        original.setNotes("Good boot space. Wife prefers lighter steering.");

        repository.save(original);
        Vehicle loaded = repository.findById(original.getId()).orElseThrow();

        assertEquals(original.getNotes(), loaded.getNotes());
    }

    @Test
    void saveAndLoad_minimalVehicle_handlesNullNestedObjects() throws IOException {
        Vehicle original = TestFixtures.minimalVehicle();

        repository.save(original);
        Vehicle loaded = repository.findById(original.getId()).orElseThrow();

        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getMake(), loaded.getMake());
        assertEquals(original.getModel(), loaded.getModel());
        assertEquals(original.getStatus(), loaded.getStatus());
        assertNull(loaded.getDerivative());
        assertNull(loaded.getModelYear());
        assertNull(loaded.getBodyType());
        assertNull(loaded.getEngine());
        assertNull(loaded.getTransmission());
        assertNull(loaded.getPerformance());
        assertNull(loaded.getDimensions());
        assertNull(loaded.getTowing());
        assertNull(loaded.getWheels());
        assertNull(loaded.getInfotainment());
        assertNull(loaded.getEconomy());
        assertNull(loaded.getSafety());
        assertNull(loaded.getFeatures());
        assertNull(loaded.getOwnership());
        assertNull(loaded.getPricing());
        assertNull(loaded.getSource());
        assertNull(loaded.getDerivedMetrics());
    }

    @Test
    void findAll_returnsAllSavedVehicles() throws IOException {
        Vehicle first = TestFixtures.fullVehicle();
        Vehicle second = TestFixtures.minimalVehicle();

        repository.save(first);
        repository.save(second);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void delete_removesFile() throws IOException {
        Vehicle vehicle = TestFixtures.fullVehicle();
        repository.save(vehicle);

        assertTrue(repository.exists(vehicle.getId()));

        repository.delete(vehicle.getId());

        assertFalse(repository.exists(vehicle.getId()));
        assertTrue(repository.findById(vehicle.getId()).isEmpty());
    }

    @Test
    void findById_missing_returnsEmpty() throws IOException {
        assertTrue(repository.findById(TestFixtures.VEHICLE_ID).isEmpty());
    }

    private static void assertVehiclesEqual(Vehicle expected, Vehicle actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMake(), actual.getMake());
        assertEquals(expected.getModel(), actual.getModel());
        assertEquals(expected.getDerivative(), actual.getDerivative());
        assertEquals(expected.getModelYear(), actual.getModelYear());
        assertEquals(expected.getBodyType(), actual.getBodyType());
        assertEquals(expected.getStatus(), actual.getStatus());

        assertEquals(expected.getEngine().getFuelType(), actual.getEngine().getFuelType());
        assertEquals(expected.getEngine().getDisplacementCc(), actual.getEngine().getDisplacementCc());
        assertEquals(expected.getEngine().getCylinders(), actual.getEngine().getCylinders());
        assertEquals(expected.getEngine().getPowerKw(), actual.getEngine().getPowerKw());
        assertEquals(expected.getEngine().getTorqueNm(), actual.getEngine().getTorqueNm());
        assertEquals(expected.getEngine().getAspiration(), actual.getEngine().getAspiration());
        assertEquals(expected.getEngine().getHybrid(), actual.getEngine().getHybrid());
        assertEquals(expected.getEngine().getPhev(), actual.getEngine().getPhev());

        assertEquals(expected.getTransmission().getType(), actual.getTransmission().getType());
        assertEquals(expected.getTransmission().getGears(), actual.getTransmission().getGears());
        assertEquals(expected.getTransmission().getDrivetrain(), actual.getTransmission().getDrivetrain());

        assertEquals(expected.getPerformance().getZeroToHundredSeconds(),
                actual.getPerformance().getZeroToHundredSeconds());
        assertEquals(expected.getPerformance().getTopSpeedKmh(), actual.getPerformance().getTopSpeedKmh());

        assertEquals(expected.getDimensions().getLengthMm(), actual.getDimensions().getLengthMm());
        assertEquals(expected.getDimensions().getTurningCircleM(), actual.getDimensions().getTurningCircleM());
        assertEquals(expected.getDimensions().getBootLitres(), actual.getDimensions().getBootLitres());

        assertEquals(expected.getTowing().getTowingBrakedKg(), actual.getTowing().getTowingBrakedKg());
        assertEquals(expected.getWheels().getTyreSize(), actual.getWheels().getTyreSize());
        assertEquals(expected.getInfotainment().getInfotainmentScreenSizeInches(),
                actual.getInfotainment().getInfotainmentScreenSizeInches());
        assertEquals(expected.getInfotainment().getSpeakerCount(), actual.getInfotainment().getSpeakerCount());

        assertEquals(expected.getEconomy().getFuelConsumptionCombined(), actual.getEconomy().getFuelConsumptionCombined());
        assertEquals(expected.getEconomy().getCo2Gkm(), actual.getEconomy().getCo2Gkm());

        assertEquals(expected.getSafety().getNcapStars(), actual.getSafety().getNcapStars());
        assertEquals(expected.getSafety().getAeb(), actual.getSafety().getAeb());
        assertEquals(expected.getSafety().getBlindSpotMonitoring(), actual.getSafety().getBlindSpotMonitoring());

        assertEquals(expected.getFeatures().getAndroidAuto(), actual.getFeatures().getAndroidAuto());
        assertEquals(expected.getFeatures().getWirelessCharging(), actual.getFeatures().getWirelessCharging());
        assertEquals(expected.getFeatures().getClimateControlType(), actual.getFeatures().getClimateControlType());
        assertEquals(expected.getFeatures().getSunroof(), actual.getFeatures().getSunroof());
        assertEquals(expected.getFeatures().getPremiumAudio(), actual.getFeatures().getPremiumAudio());

        assertEquals(expected.getOwnership().getWarrantyYears(), actual.getOwnership().getWarrantyYears());
        assertEquals(expected.getOwnership().getServiceIntervalKm(), actual.getOwnership().getServiceIntervalKm());
        assertEquals(expected.getOwnership().getMaintenancePlanYears(), actual.getOwnership().getMaintenancePlanYears());
        assertEquals(expected.getOwnership().getMaintenancePlanKm(), actual.getOwnership().getMaintenancePlanKm());
        assertEquals(expected.getOwnership().getPartsSupportScore(), actual.getOwnership().getPartsSupportScore());
        assertEquals(expected.getOwnership().getLocalProduction(), actual.getOwnership().getLocalProduction());

        assertEquals(0, expected.getPricing().getListPrice().compareTo(actual.getPricing().getListPrice()));
        assertEquals(expected.getPricing().getListPriceDate(), actual.getPricing().getListPriceDate());

        assertEquals(expected.getSource().getSourceType(), actual.getSource().getSourceType());
        assertEquals(expected.getSource().getSourceName(), actual.getSource().getSourceName());
        assertEquals(expected.getSource().getSourceUrl(), actual.getSource().getSourceUrl());
        assertEquals(expected.getSource().getImportedDate(), actual.getSource().getImportedDate());

        assertEquals(expected.getDerivedMetrics().getSafetyScore(), actual.getDerivedMetrics().getSafetyScore());
        assertEquals(expected.getDerivedMetrics().getOverallScore(), actual.getDerivedMetrics().getOverallScore());
    }
}
