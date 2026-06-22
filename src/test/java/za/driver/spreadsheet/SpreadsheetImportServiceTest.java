package za.driver.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.Vehicle;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringOverrides;
import za.driver.scoring.ScoringService;
import za.driver.scoring.ScoringTestFixtures;
import za.driver.service.VehicleService;

class SpreadsheetImportServiceTest {

    private static final UUID VEHICLE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @TempDir
    Path tempDir;

    private VehicleRepository repository;
    private SpreadsheetExportService exportService;
    private SpreadsheetImportService importService;
    private VehicleService vehicleService;

    @BeforeEach
    void setUp() throws IOException {
        repository = new VehicleRepository(tempDir);
        exportService = new SpreadsheetExportService();
        importService = new SpreadsheetImportService(repository);
        vehicleService = new VehicleService(repository, new ScoringService());
        repository.save(ScoringTestFixtures.fullVehicle());
    }

    @Test
    void export_producesReadableCsv() throws IOException {
        Path file = tempDir.resolve("export.csv");
        exportService.export(file, List.of(ScoringTestFixtures.fullVehicle()));

        SpreadsheetDataSheet sheet = new CsvSpreadsheetReader().readDataSheet(file);
        assertEquals(VehicleSpreadsheetSchema.headers(), sheet.headers());
        assertEquals(1, sheet.rows().size());
        assertEquals(VEHICLE_ID.toString(), sheet.rows().get(0).get("id"));
    }

    @Test
    void import_mergesPricingOnly_preservesOtherFields() throws IOException {
        Path file = tempDir.resolve("pricing-update.csv");
        Vehicle original = ScoringTestFixtures.fullVehicle();
        exportService.export(file, List.of(original));

        SpreadsheetDataSheet sheet = new CsvSpreadsheetReader().readDataSheet(file);
        var row = sheet.rows().get(0);
        row.put("pricing.priceZar", "399000");
        writeSingleRowExport(file, sheet.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        vehicleService.importSpreadsheetUpdates(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(VEHICLE_ID).orElseThrow();
        assertEquals(new BigDecimal("399000"), loaded.getPricing().getPriceZar());
        assertEquals("Toyota", loaded.getMake());
        assertEquals(original.getEngine().getPowerKw(), loaded.getEngine().getPowerKw());
    }

    @Test
    void import_mergesManualScoresOnly_preservesOtherFieldsAndUnsetOverride() throws IOException {
        Vehicle original = ScoringTestFixtures.fullVehicle();
        vehicleService.save(original, ScoringTestFixtures.familyFocusedProfile(), ScoringOverrides.of(80.0, 70.0));

        Path file = tempDir.resolve("scores-update.csv");
        exportService.export(file, List.of(repository.findById(VEHICLE_ID).orElseThrow()));

        SpreadsheetDataSheet sheet = new CsvSpreadsheetReader().readDataSheet(file);
        var row = sheet.rows().get(0);
        row.put("derivedMetrics.reliabilityScore", "90");
        writeSingleRowExport(file, sheet.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.getEntries().get(0).changedFieldCount());
        vehicleService.importSpreadsheetUpdates(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(VEHICLE_ID).orElseThrow();
        assertEquals(90.0, loaded.getDerivedMetrics().getReliabilityScore());
        assertEquals(70.0, loaded.getDerivedMetrics().getPrestigeScore());
        assertEquals("Toyota", loaded.getMake());
        assertEquals(original.getEngine().getPowerKw(), loaded.getEngine().getPowerKw());
    }

    @Test
    void import_unknownUuid_failsValidation() throws IOException {
        Path file = tempDir.resolve("unknown.csv");
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        exportService.export(file, List.of(vehicle));

        SpreadsheetDataSheet sheet = new CsvSpreadsheetReader().readDataSheet(file);
        var row = sheet.rows().get(0);
        row.put("id", UUID.randomUUID().toString());
        row.put("pricing.priceZar", "123456");
        writeSingleRowExport(file, sheet.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertFalse(preview.isValid());
        assertTrue(preview.getErrors().stream().anyMatch(error -> error.contains("unknown vehicle id")));
    }

    private void writeSingleRowExport(Path file, List<String> headers, List<java.util.Map<String, String>> rows)
            throws IOException {
        List<List<String>> dataRows = new java.util.ArrayList<>();
        for (var row : rows) {
            dataRows.add(headers.stream().map(header -> row.getOrDefault(header, "")).toList());
        }
        new CsvSpreadsheetWriter().write(file, headers, dataRows);
    }
}
