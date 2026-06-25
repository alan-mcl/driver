package za.driver.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import za.driver.import_.ImportResult;
import za.driver.import_.ImportVehicleEntry;
import za.driver.import_.VehicleImportMerger;
import za.driver.model.DataQuality;
import za.driver.model.ScoringProfile;
import za.driver.model.Source;
import za.driver.model.SourceType;
import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ManualScoreOverrideUtil;
import za.driver.scoring.ScoringOverrides;
import za.driver.scoring.ScoringService;
import za.driver.spreadsheet.SpreadsheetImportResult;

public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ScoringService scoringService;

    public VehicleService(VehicleRepository vehicleRepository, ScoringService scoringService) {
        this.vehicleRepository = vehicleRepository;
        this.scoringService = scoringService;
    }

    public List<Vehicle> findAll() throws IOException {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> findAll(ScoringProfile profile) throws IOException {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        for (Vehicle vehicle : vehicles) {
            applyCalculatedMetrics(vehicle, profile);
        }
        return vehicles;
    }

    public Optional<Vehicle> findById(UUID id) throws IOException {
        return vehicleRepository.findById(id);
    }

    public Optional<Vehicle> findById(UUID id, ScoringProfile profile) throws IOException {
        return vehicleRepository.findById(id).map(vehicle -> {
            applyCalculatedMetrics(vehicle, profile);
            return vehicle;
        });
    }

    public Optional<Vehicle> findByIdentity(String make, String model, String derivative) throws IOException {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> VehicleIdentity.matches(make, model, derivative,
                        vehicle.getMake(), vehicle.getModel(), vehicle.getDerivative()))
                .findFirst();
    }

    public void delete(UUID id) throws IOException {
        vehicleRepository.delete(id);
    }

    public Vehicle save(Vehicle vehicle, ScoringProfile profile) throws IOException {
        return save(vehicle, profile, ScoringOverrides.none());
    }

    public Vehicle save(Vehicle vehicle, ScoringProfile profile, ScoringOverrides overrides) throws IOException {
        ManualScoreOverrideUtil.applyOverrides(vehicle, overrides);
        vehicle.setDerivedMetrics(null);
        enrichSource(vehicle);
        ScoringOverrides effectiveOverrides = ScoringOverrides.fromVehicle(vehicle);
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, effectiveOverrides));
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    public Vehicle importAndSave(ImportResult result, ScoringProfile profile) throws IOException {
        return importAndSave(result, profile, ScoringOverrides.none());
    }

    public Vehicle importAndSave(ImportResult result, ScoringProfile profile, ScoringOverrides overrides)
            throws IOException {
        List<Vehicle> saved = importAllAndSave(result, profile, overrides);
        return saved.get(0);
    }

    public List<Vehicle> importAllAndSave(ImportResult result, ScoringProfile profile) throws IOException {
        return importAllAndSave(result, profile, ScoringOverrides.none());
    }

    public List<Vehicle> importAllAndSave(ImportResult result, ScoringProfile profile, ScoringOverrides overrides)
            throws IOException {
        if (result == null || !result.isValid()) {
            String message = result == null ? "Import result is required" : String.join("; ", result.getErrors());
            throw new IllegalArgumentException(message);
        }

        List<Vehicle> saved = new ArrayList<>();
        for (ImportVehicleEntry entry : result.getEntries()) {
            saved.add(importEntry(entry, profile, overrides));
        }
        return saved;
    }

    public List<Vehicle> importSpreadsheet(SpreadsheetImportResult result, ScoringProfile profile) throws IOException {
        return importSpreadsheet(result, profile, ScoringOverrides.none());
    }

    public List<Vehicle> importSpreadsheet(
            SpreadsheetImportResult result,
            ScoringProfile profile,
            ScoringOverrides overrides) throws IOException {
        if (result == null || !result.isValid()) {
            String message = result == null ? "Import result is required" : String.join("; ", result.getErrors());
            throw new IllegalArgumentException(message);
        }

        List<Vehicle> saved = new ArrayList<>();
        for (SpreadsheetImportResult.SpreadsheetImportEntry entry : result.getEntries()) {
            if (entry.action() == SpreadsheetImportResult.ImportAction.CREATE) {
                saved.add(importSpreadsheetCreate(entry, profile, overrides));
            } else {
                saved.add(importSpreadsheetUpdate(entry, profile, overrides));
            }
        }
        return saved;
    }

    public List<Vehicle> importSpreadsheetUpdates(SpreadsheetImportResult result, ScoringProfile profile)
            throws IOException {
        return importSpreadsheet(result, profile);
    }

    public List<Vehicle> importSpreadsheetUpdates(
            SpreadsheetImportResult result,
            ScoringProfile profile,
            ScoringOverrides overrides) throws IOException {
        return importSpreadsheet(result, profile, overrides);
    }

    private Vehicle importSpreadsheetCreate(
            SpreadsheetImportResult.SpreadsheetImportEntry entry,
            ScoringProfile profile,
            ScoringOverrides overrides) throws IOException {
        Vehicle vehicle = entry.vehicle();
        vehicle.setId(entry.vehicleId());
        vehicle.setDerivedMetrics(null);
        enrichSource(vehicle);
        vehicle.getSource().setImportedDate(LocalDateTime.now());
        ScoringOverrides mergedOverrides = ScoringOverrides.merge(entry.scoringOverrides(), overrides);
        ManualScoreOverrideUtil.applyOverrides(vehicle, mergedOverrides);
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, ScoringOverrides.fromVehicle(vehicle)));
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    private Vehicle importSpreadsheetUpdate(
            SpreadsheetImportResult.SpreadsheetImportEntry entry,
            ScoringProfile profile,
            ScoringOverrides overrides) throws IOException {
        Vehicle existing = vehicleRepository.findById(entry.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown vehicle id: " + entry.vehicleId()));
        Vehicle merged = VehicleImportMerger.merge(existing, entry.vehicle());
        ScoringOverrides existingOverrides = ScoringOverrides.fromVehicle(existing);
        ScoringOverrides rowOverrides = ScoringOverrides.merge(existingOverrides, entry.scoringOverrides());
        ScoringOverrides mergedOverrides = ScoringOverrides.merge(rowOverrides, overrides);
        ManualScoreOverrideUtil.applyOverrides(merged, mergedOverrides);
        return save(merged, profile, ScoringOverrides.none());
    }

    private Vehicle importEntry(ImportVehicleEntry entry, ScoringProfile profile, ScoringOverrides overrides)
            throws IOException {
        Vehicle vehicle = entry.getVehicle();
        vehicle.setDerivedMetrics(null);

        Map<String, DataQuality> dataQuality = entry.getDataQuality();
        Optional<Vehicle> existing = findByIdentity(vehicle.getMake(), vehicle.getModel(), vehicle.getDerivative());
        if (existing.isPresent()) {
            Vehicle merged = VehicleImportMerger.merge(existing.get(), vehicle);
            if (!dataQuality.isEmpty()) {
                merged.setDataQuality(VehicleImportMerger.mergeDataQuality(merged.getDataQuality(), dataQuality));
            }
            vehicle = merged;
        } else if (!dataQuality.isEmpty()) {
            vehicle.setDataQuality(dataQuality);
        }

        enrichSource(vehicle);
        vehicle.getSource().setImportedDate(LocalDateTime.now());
        ManualScoreOverrideUtil.applyOverrides(vehicle, overrides);
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, ScoringOverrides.fromVehicle(vehicle)));
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    static void enrichSource(Vehicle vehicle) {
        Source source = vehicle.getSource();
        if (source == null) {
            source = new Source();
            source.setSourceType(SourceType.OTHER);
            vehicle.setSource(source);
        }
        if (source.getImportedDate() == null) {
            source.setImportedDate(LocalDateTime.now());
        }
    }

    private void applyCalculatedMetrics(Vehicle vehicle, ScoringProfile profile) {
        if (vehicle == null || profile == null) {
            return;
        }
        vehicle.setDerivedMetrics(
                scoringService.calculate(vehicle, profile, ScoringOverrides.fromVehicle(vehicle)));
    }
}
