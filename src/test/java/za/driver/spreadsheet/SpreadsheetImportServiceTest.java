package za.driver.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
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
        vehicleService = new VehicleService(repository, new ScoringService());
        importService = new SpreadsheetImportService(vehicleService);
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
        row.put("pricing.listPriceZar", "399000");
        writeRows(file, sheet.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.updateCount());
        assertEquals(0, preview.createCount());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(VEHICLE_ID).orElseThrow();
        assertEquals(new BigDecimal("399000"), loaded.getPricing().getListPriceZar());
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
        row.put("manualScoreOverrides.reliabilityManualEstimate", "90");
        writeRows(file, sheet.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.getEntries().get(0).changedFieldCount());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(VEHICLE_ID).orElseThrow();
        assertEquals(90.0, loaded.getManualScoreOverrides().getReliabilityManualEstimate());
        assertEquals(91.0, loaded.getDerivedMetrics().getReliabilityScore());
        assertEquals(70.0, loaded.getDerivedMetrics().getPrestigeScore());
        assertEquals("Toyota", loaded.getMake());
        assertEquals(original.getEngine().getPowerKw(), loaded.getEngine().getPowerKw());
    }

    @Test
    void import_unknownUuid_createsNewVehicle() throws IOException {
        Path file = tempDir.resolve("unknown.csv");
        UUID newId = UUID.randomUUID();
        Map<String, String> row = blankRow();
        row.put("id", newId.toString());
        row.put("make", "Honda");
        row.put("model", "Civic");
        row.put("derivative", "1.5T Sport");
        row.put("pricing.listPriceZar", "123456");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.createCount());
        assertEquals(newId, preview.getEntries().get(0).vehicleId());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(newId).orElseThrow();
        assertEquals("Honda", loaded.getMake());
        assertEquals("Civic", loaded.getModel());
        assertEquals(new BigDecimal("123456"), loaded.getPricing().getListPriceZar());
        assertNotNull(loaded.getDerivedMetrics());
    }

    @Test
    void import_createsNewVehicle_whenIdBlank() throws IOException {
        Path file = tempDir.resolve("create-blank-id.csv");
        Map<String, String> row = blankRow();
        row.put("make", "Honda");
        row.put("model", "Fit");
        row.put("pricing.listPriceZar", "250000");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.createCount());
        assertEquals(SpreadsheetImportResult.ImportAction.CREATE, preview.getEntries().get(0).action());
        UUID assignedId = preview.getEntries().get(0).vehicleId();
        assertNotNull(assignedId);
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(assignedId).orElseThrow();
        assertEquals("Honda", loaded.getMake());
        assertEquals("Fit", loaded.getModel());
        assertEquals(new BigDecimal("250000"), loaded.getPricing().getListPriceZar());
        assertNotNull(loaded.getDerivedMetrics());
    }

    @Test
    void import_createsNewVehicle_withExplicitUuid() throws IOException {
        Path file = tempDir.resolve("create-explicit-id.csv");
        UUID newId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Map<String, String> row = blankRow();
        row.put("id", newId.toString());
        row.put("make", "Mazda");
        row.put("model", "3");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(newId, preview.getEntries().get(0).vehicleId());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(newId).orElseThrow();
        assertEquals("Mazda", loaded.getMake());
        assertEquals("3", loaded.getModel());
    }

    @Test
    void import_updatesExisting_byIdentity_whenIdBlank() throws IOException {
        Path file = tempDir.resolve("update-by-identity.csv");
        Map<String, String> row = blankRow();
        row.put("make", "Toyota");
        row.put("model", "Corolla");
        row.put("derivative", "1.8 XS");
        row.put("pricing.listPriceZar", "355000");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.updateCount());
        assertEquals(0, preview.createCount());
        assertEquals(VEHICLE_ID, preview.getEntries().get(0).vehicleId());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(VEHICLE_ID).orElseThrow();
        assertEquals(new BigDecimal("355000"), loaded.getPricing().getListPriceZar());
    }

    @Test
    void import_newRow_defaultsStatusToCandidate() throws IOException {
        Path file = tempDir.resolve("create-default-status.csv");
        Map<String, String> row = blankRow();
        row.put("make", "Kia");
        row.put("model", "Picanto");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(VehicleStatus.CANDIDATE, preview.getEntries().get(0).vehicle().getStatus());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        UUID assignedId = preview.getEntries().get(0).vehicleId();
        Vehicle loaded = repository.findById(assignedId).orElseThrow();
        assertEquals(VehicleStatus.CANDIDATE, loaded.getStatus());
    }

    @Test
    void import_newRow_requiresMakeAndModel() throws IOException {
        Path missingMake = tempDir.resolve("missing-make.csv");
        Map<String, String> rowWithoutMake = blankRow();
        rowWithoutMake.put("model", "Civic");
        writeRows(missingMake, VehicleSpreadsheetSchema.headers(), List.of(rowWithoutMake));

        SpreadsheetImportResult missingMakePreview = importService.preview(missingMake);
        assertFalse(missingMakePreview.isValid());
        assertTrue(missingMakePreview.getErrors().stream().anyMatch(error -> error.contains("make is required")));

        Path missingModel = tempDir.resolve("missing-model.csv");
        Map<String, String> rowWithoutModel = blankRow();
        rowWithoutModel.put("make", "Honda");
        writeRows(missingModel, VehicleSpreadsheetSchema.headers(), List.of(rowWithoutModel));

        SpreadsheetImportResult missingModelPreview = importService.preview(missingModel);
        assertFalse(missingModelPreview.isValid());
        assertTrue(missingModelPreview.getErrors().stream().anyMatch(error -> error.contains("model is required")));
    }

    @Test
    void import_conflictingId_failsValidation() throws IOException {
        Path file = tempDir.resolve("conflicting-id.csv");
        Map<String, String> row = blankRow();
        row.put("id", VEHICLE_ID.toString());
        row.put("make", "Honda");
        row.put("model", "Civic");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertFalse(preview.isValid());
        assertTrue(preview.getErrors().stream().anyMatch(error -> error.contains("already exists")));
    }

    @Test
    void import_duplicateIdentitiesInFile_failsValidation() throws IOException {
        Path file = tempDir.resolve("duplicate-identities.csv");
        Map<String, String> first = blankRow();
        first.put("make", "Honda");
        first.put("model", "Civic");
        Map<String, String> second = blankRow();
        second.put("make", "Honda");
        second.put("model", "Civic");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(first, second));

        SpreadsheetImportResult preview = importService.preview(file);
        assertFalse(preview.isValid());
        assertTrue(preview.getErrors().stream().anyMatch(error -> error.contains("duplicate identity")));
    }

    @Test
    void import_create_appliesManualScoreOverrides() throws IOException {
        Path file = tempDir.resolve("create-with-scores.csv");
        Map<String, String> row = blankRow();
        row.put("make", "BMW");
        row.put("model", "3 Series");
        row.put("manualScoreOverrides.reliabilityManualEstimate", "75");
        row.put("manualScoreOverrides.prestigeScore", "85");
        writeRows(file, VehicleSpreadsheetSchema.headers(), List.of(row));

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        vehicleService.importSpreadsheet(preview, ScoringTestFixtures.familyFocusedProfile());

        Vehicle loaded = repository.findById(preview.getEntries().get(0).vehicleId()).orElseThrow();
        assertEquals(75.0, loaded.getManualScoreOverrides().getReliabilityManualEstimate());
        assertEquals(75.0, loaded.getDerivedMetrics().getReliabilityScore());
        assertEquals(85.0, loaded.getDerivedMetrics().getPrestigeScore());
    }

    @Test
    void preview_reportsProgress() throws IOException {
        Path file = tempDir.resolve("progress.csv");
        exportService.export(file, List.of(ScoringTestFixtures.fullVehicle()));

        List<int[]> updates = new ArrayList<>();
        importService.preview(file, (current, total) -> updates.add(new int[] {current, total}));

        assertFalse(updates.isEmpty());
        assertEquals(0, updates.get(0)[0]);
        assertEquals(1, updates.get(0)[1]);
        assertEquals(1, updates.get(updates.size() - 1)[0]);
    }

    @Test
    void export_thenImportWithUtf8Bom_succeeds() throws IOException {
        Path file = tempDir.resolve("export-bom.csv");
        Vehicle original = ScoringTestFixtures.fullVehicle();
        exportService.export(file, List.of(original));
        String content = java.nio.file.Files.readString(file);
        java.nio.file.Files.writeString(file, "\uFEFF" + content);

        SpreadsheetImportResult preview = importService.preview(file);
        assertTrue(preview.isValid());
        assertEquals(1, preview.updateCount());
    }

    private static Map<String, String> blankRow() {
        Map<String, String> row = new LinkedHashMap<>();
        for (String header : VehicleSpreadsheetSchema.headers()) {
            row.put(header, "");
        }
        return row;
    }

    private void writeRows(Path file, List<String> headers, List<Map<String, String>> rows) throws IOException {
        List<List<String>> dataRows = new ArrayList<>();
        for (var row : rows) {
            dataRows.add(headers.stream().map(header -> row.getOrDefault(header, "")).toList());
        }
        new CsvSpreadsheetWriter().write(file, headers, dataRows);
    }
}
