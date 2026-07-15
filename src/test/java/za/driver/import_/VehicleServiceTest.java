package za.driver.import_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.DataQuality;
import za.driver.model.DerivedMetrics;
import za.driver.model.DrivetrainType;
import za.driver.model.ScoringProfile;
import za.driver.model.SourceType;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringService;
import za.driver.scoring.ScoringTestFixtures;
import za.driver.service.VehicleService;

class VehicleServiceTest {

    private static final UUID VEHICLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @TempDir
    Path tempDir;

    private ImportService importService;
    private VehicleService vehicleService;
    private VehicleRepository vehicleRepository;
    private ScoringProfile profile;

    @BeforeEach
    void setUp() {
        vehicleRepository = new VehicleRepository(tempDir);
        vehicleService = new VehicleService(vehicleRepository, new ScoringService());
        importService = new ImportService();
        profile = ScoringTestFixtures.familyFocusedProfile();
    }

    @Test
    void importAndSave_validVehicle_persistsToRepository() throws IOException {
        ImportResult result = importService.parse(fullImportJson());

        vehicleService.importAndSave(result, profile);

        Optional<Vehicle> loaded = vehicleRepository.findById(VEHICLE_ID);
        assertTrue(loaded.isPresent());
        assertEquals("Toyota", loaded.get().getMake());
    }

    @Test
    void importAndSave_stampsImportedDate() throws IOException {
        ImportResult result = importService.parse(minimalImportJson());

        Vehicle saved = vehicleService.importAndSave(result, profile);

        assertNotNull(saved.getSource());
        assertNotNull(saved.getSource().getImportedDate());
    }

    @Test
    void importAndSave_preservesProvidedSource() throws IOException {
        ImportResult result = importService.parse(importJsonWithSource());

        Vehicle saved = vehicleService.importAndSave(result, profile);

        assertEquals(SourceType.WEBSITE, saved.getSource().getSourceType());
        assertEquals("Toyota SA", saved.getSource().getSourceName());
        assertEquals("https://www.toyota.co.za", saved.getSource().getSourceUrl());
        assertNotNull(saved.getSource().getImportedDate());
    }

    @Test
    void importAndSave_recalculatesDerivedMetrics() throws IOException {
        ImportResult result = importService.parse(importJsonWithStaleDerivedMetrics());

        Vehicle saved = vehicleService.importAndSave(result, profile);

        assertNotNull(saved.getDerivedMetrics());
        assertNotNull(saved.getDerivedMetrics().getSafetyScore());
        assertTrue(saved.getDerivedMetrics().getSafetyScore() > 0.0);
        assertTrue(saved.getDerivedMetrics().getSafetyScore() < 100.0);
    }

    @Test
    void importAndSave_preservesDataQuality() throws IOException {
        ImportResult result = importService.parse(importJsonWithDataQuality());

        Vehicle saved = vehicleService.importAndSave(result, profile);

        assertEquals(DataQuality.VERIFIED, saved.getDataQuality().get("pricing.listPriceZar"));
        assertEquals(DataQuality.ESTIMATED, saved.getDataQuality().get("engine.powerKw"));
    }

    @Test
    void importAndSave_invalidResult_throws() {
        ImportResult result = ImportResult.failure(java.util.List.of("Vehicle make is required"));

        assertThrows(IllegalArgumentException.class, () -> vehicleService.importAndSave(result, profile));
    }

    @Test
    void importAndSave_matchingIdentity_updatesExistingRecord() throws IOException {
        ImportResult initial = importService.parse(fullImportJson());
        Vehicle saved = vehicleService.importAndSave(initial, profile);
        saved.setStatus(VehicleStatus.SHORTLISTED);
        vehicleService.save(saved, profile);

        ImportResult update = importService.parse(importJsonForUpdate());
        Vehicle updated = vehicleService.importAndSave(update, profile);

        assertEquals(saved.getId(), updated.getId());
        assertEquals(VehicleStatus.SHORTLISTED, updated.getStatus());
        assertEquals(350000, updated.getPricing().getListPriceZar().intValue());
        assertEquals(110.0, updated.getEngine().getPowerKw());
    }

    @Test
    void delete_removesVehicle() throws IOException {
        ImportResult result = importService.parse(minimalImportJson());
        Vehicle saved = vehicleService.importAndSave(result, profile);

        vehicleService.delete(saved.getId());

        assertTrue(vehicleRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    void findAll_withProfile_recalculatesStaleDerivedMetrics() throws IOException {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        DerivedMetrics stale = new DerivedMetrics();
        stale.setSafetyScore(80.0);
        stale.setOverallScore(75.0);
        vehicle.setDerivedMetrics(stale);
        vehicleRepository.save(vehicle);

        Vehicle loaded = vehicleService.findAll(profile).get(0);

        assertNotNull(loaded.getDerivedMetrics().getAwesomenessScore());
        assertNotNull(loaded.getDerivedMetrics().getSafetyScore());
    }

    @Test
    void importAllAndSave_multipleVehicles_persistsAll() throws IOException {
        ImportResult result = importService.parse(batchImportJson());

        List<Vehicle> saved = vehicleService.importAllAndSave(result, profile);

        assertEquals(2, saved.size());
        assertEquals(2, vehicleRepository.findAll().size());
        assertTrue(vehicleRepository.findById(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000")).isPresent());
        assertTrue(vehicleRepository.findById(
                UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")).isPresent());
    }

    @Test
    void importAndSave_matchingIdentity_mergesNewFieldsWithoutClearingExisting() throws IOException {
        ImportResult initial = importService.parse(fullImportJson());
        Vehicle saved = vehicleService.importAndSave(initial, profile);
        assertEquals(103.0, saved.getEngine().getPowerKw());

        ImportResult update = importService.parse(importJsonWithNewFields());
        Vehicle updated = vehicleService.importAndSave(update, profile);

        assertEquals(saved.getId(), updated.getId());
        assertEquals(103.0, updated.getEngine().getPowerKw());
        assertEquals(DrivetrainType.AWD, updated.getTransmission().getDrivetrain());
        assertEquals(8.4, updated.getPerformance().getZeroToHundredSeconds());
        assertEquals(1500, updated.getTowing().getTowingBrakedKg());
        assertEquals(1300, updated.getDimensions().getKerbWeightKg());
    }

    private static String importJsonWithNewFields() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                    "make": "Toyota",
                    "model": "Corolla",
                    "derivative": "1.8 XS",
                    "status": "CANDIDATE",
                    "transmission": { "drivetrain": "AWD" },
                    "performance": { "zeroToHundredSeconds": 8.4, "topSpeedKmh": 210 },
                    "towing": { "towingBrakedKg": 1500 },
                    "wheels": { "tyreSize": "215/55 R17" },
                    "infotainment": { "infotainmentScreenSizeInches": 10.25, "speakerCount": 8 },
                    "features": { "climateControlType": "DUAL_ZONE_AUTO", "sunroof": true, "premiumAudio": true },
                    "ownership": { "maintenancePlanYears": 5, "maintenancePlanKm": 90000 }
                  }
                }
                """;
    }

    private static String batchImportJson() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicles": [
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "make": "Toyota",
                      "model": "Corolla",
                      "status": "CANDIDATE"
                    },
                    {
                      "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                      "make": "Honda",
                      "model": "Civic",
                      "status": "CANDIDATE"
                    }
                  ]
                }
                """;
    }

    private static String importJsonForUpdate() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                    "make": "Toyota",
                    "model": "Corolla",
                    "derivative": "1.8 XS",
                    "status": "CANDIDATE",
                    "engine": { "powerKw": 110.0 }
                  }
                }
                """;
    }

    private static String minimalImportJson() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "CANDIDATE"
                  }
                }
                """;
    }

    private static String fullImportJson() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "%s",
                    "make": "%s",
                    "model": "%s",
                    "derivative": "1.8 XS",
                    "status": "CANDIDATE",
                    "engine": { "powerKw": 103.0, "torqueNm": 173.0 },
                    "dimensions": { "lengthMm": 4630, "kerbWeightKg": 1300, "seats": 5, "wheelbaseMm": 2700, "turningCircleM": 10.8 },
                    "economy": { "fuelConsumptionCombined": 6.5 },
                    "safety": { "ncapStars": 5, "airbags": 7, "abs": true, "esp": true, "aeb": true, "laneAssist": true, "adaptiveCruiseControl": true, "rearCrossTrafficAlert": true },
                    "features": { "androidAuto": true, "appleCarplay": true, "reverseCamera": true, "parkingSensorsRear": true, "climateControl": true, "keylessEntry": true, "pushButtonStart": true },
                    "ownership": { "warrantyYears": 3, "serviceIntervalKm": 15000 },
                    "pricing": { "priceZar": 350000, "priceDate": "2026-06-17" }
                  }
                }
                """.formatted(vehicle.getId(), vehicle.getMake(), vehicle.getModel());
    }

    private static String importJsonWithSource() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "CANDIDATE",
                    "source": {
                      "sourceType": "WEBSITE",
                      "sourceName": "Toyota SA",
                      "sourceUrl": "https://www.toyota.co.za"
                    }
                  }
                }
                """;
    }

    private static String importJsonWithStaleDerivedMetrics() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "CANDIDATE",
                    "safety": { "ncapStars": 5, "airbags": 7, "abs": true, "esp": true, "aeb": true },
                    "derivedMetrics": {
                      "safetyScore": 1.0,
                      "overallScore": 1.0
                    }
                  }
                }
                """;
    }

    private static String importJsonWithDataQuality() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "CANDIDATE"
                  },
                  "dataQuality": {
                    "pricing.priceZar": "VERIFIED",
                    "engine.powerKw": "ESTIMATED"
                  }
                }
                """;
    }
}
