package za.driver.spreadsheet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;
import za.driver.model.VehicleStatus;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringOverrides;
import za.driver.service.VehicleService;

public final class SpreadsheetImportService {

    private final CsvSpreadsheetReader reader;
    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;

    public SpreadsheetImportService(VehicleService vehicleService) {
        this(new CsvSpreadsheetReader(), vehicleService);
    }

    SpreadsheetImportService(CsvSpreadsheetReader reader, VehicleService vehicleService) {
        this.reader = reader;
        this.vehicleService = vehicleService;
        this.vehicleRepository = null;
    }

    SpreadsheetImportService(CsvSpreadsheetReader reader, VehicleRepository vehicleRepository) {
        this.reader = reader;
        this.vehicleService = null;
        this.vehicleRepository = vehicleRepository;
    }

    public SpreadsheetImportResult preview(Path file) throws IOException {
        SpreadsheetDataSheet sheet = reader.readDataSheet(file);
        SpreadsheetImportResult.Builder builder = new SpreadsheetImportResult.Builder();
        Map<String, Integer> seenIdentities = new HashMap<>();

        for (int index = 0; index < sheet.rows().size(); index++) {
            Map<String, String> row = sheet.rows().get(index);
            int rowNumber = index + 2;
            if (!VehicleSpreadsheetMapper.hasImportableData(row)) {
                continue;
            }

            Vehicle partial;
            try {
                partial = VehicleSpreadsheetMapper.fromRowMap(row, true);
            } catch (IllegalArgumentException ex) {
                builder.addError("Row " + rowNumber + ": " + ex.getMessage());
                continue;
            }

            if (partial.getMake() == null || partial.getMake().isBlank()) {
                builder.addError("Row " + rowNumber + ": make is required");
                continue;
            }
            if (partial.getModel() == null || partial.getModel().isBlank()) {
                builder.addError("Row " + rowNumber + ": model is required");
                continue;
            }

            String identityKey = identityKey(partial);
            Integer firstRow = seenIdentities.putIfAbsent(identityKey, rowNumber);
            if (firstRow != null) {
                builder.addError("Row " + rowNumber + ": duplicate identity matches row " + firstRow);
                continue;
            }

            Optional<Vehicle> existingByIdentity = findByIdentity(
                    partial.getMake(), partial.getModel(), partial.getDerivative());
            ScoringOverrides scoringOverrides = VehicleSpreadsheetMapper.scoringOverridesFromRow(row);

            if (existingByIdentity.isPresent()) {
                Vehicle existing = existingByIdentity.get();
                Vehicle updatePartial = VehicleSpreadsheetMapper.fromRowMap(row, false);
                int changedFields = countChangedFields(existing, updatePartial);
                builder.addEntry(new SpreadsheetImportResult.SpreadsheetImportEntry(
                        SpreadsheetImportResult.ImportAction.UPDATE,
                        existing.getId(),
                        updatePartial,
                        scoringOverrides,
                        changedFields));
                continue;
            }

            String idValue = row.getOrDefault("id", "").trim();
            UUID vehicleId;
            if (idValue.isEmpty()) {
                vehicleId = UUID.randomUUID();
            } else {
                try {
                    vehicleId = UUID.fromString(idValue);
                } catch (IllegalArgumentException ex) {
                    builder.addError("Row " + rowNumber + ": invalid id '" + idValue + "'");
                    continue;
                }
                Optional<Vehicle> existingById = findById(vehicleId);
                if (existingById.isPresent()) {
                    builder.addError("Row " + rowNumber + ": id " + vehicleId + " already exists");
                    continue;
                }
            }

            if (partial.getStatus() == null) {
                partial.setStatus(VehicleStatus.CANDIDATE);
            }
            partial.setId(vehicleId);
            int fieldCount = VehicleSpreadsheetMapper.countNonBlankSpecFields(row);
            builder.addEntry(new SpreadsheetImportResult.SpreadsheetImportEntry(
                    SpreadsheetImportResult.ImportAction.CREATE,
                    vehicleId,
                    partial,
                    scoringOverrides,
                    fieldCount));
        }

        return builder.build();
    }

    private Optional<Vehicle> findByIdentity(String make, String model, String derivative) throws IOException {
        if (vehicleService != null) {
            return vehicleService.findByIdentity(make, model, derivative);
        }
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> VehicleIdentity.matches(make, model, derivative,
                        vehicle.getMake(), vehicle.getModel(), vehicle.getDerivative()))
                .findFirst();
    }

    private Optional<Vehicle> findById(UUID id) throws IOException {
        if (vehicleService != null) {
            return vehicleService.findById(id);
        }
        return vehicleRepository.findById(id);
    }

    private static String identityKey(Vehicle vehicle) {
        return VehicleIdentity.normalize(vehicle.getMake()) + "|"
                + VehicleIdentity.normalize(vehicle.getModel()) + "|"
                + VehicleIdentity.normalize(vehicle.getDerivative());
    }

    private static int countChangedFields(Vehicle existing, Vehicle partial) {
        Map<String, String> existingRow = VehicleSpreadsheetMapper.toRowMap(existing);
        Map<String, String> partialRow = VehicleSpreadsheetMapper.toRowMap(partial);
        int count = 0;
        for (String header : VehicleSpreadsheetSchema.headers()) {
            if ("id".equals(header) || "status".equals(header)) {
                continue;
            }
            String newValue = partialRow.getOrDefault(header, "");
            if (!newValue.isBlank() && !newValue.equals(existingRow.getOrDefault(header, ""))) {
                count++;
            }
        }
        return count;
    }
}
