package za.driver.spreadsheet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
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
        return preview(file, null);
    }

    public SpreadsheetImportResult preview(Path file, SpreadsheetImportProgress progress) throws IOException {
        SpreadsheetDataSheet sheet = reader.readDataSheet(file);
        int totalRows = sheet.rows().size();
        VehicleIndex vehicleIndex = loadVehicleIndex(progress, totalRows);
        SpreadsheetImportResult.Builder builder = new SpreadsheetImportResult.Builder();
        Map<String, Integer> seenIdentities = new HashMap<>();

        for (int index = 0; index < totalRows; index++) {
            reportProgress(progress, index + 1, totalRows);
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

            Optional<Vehicle> existingByIdentity = vehicleIndex.findByIdentity(
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
                Optional<Vehicle> existingById = vehicleIndex.findById(vehicleId);
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

    private VehicleIndex loadVehicleIndex(SpreadsheetImportProgress progress, int totalRows) throws IOException {
        reportProgress(progress, 0, totalRows);
        List<Vehicle> vehicles;
        if (vehicleService != null) {
            vehicles = vehicleService.findAll();
        } else {
            vehicles = vehicleRepository.findAll();
        }
        return VehicleIndex.from(vehicles);
    }

    private static void reportProgress(SpreadsheetImportProgress progress, int current, int total) {
        if (progress != null) {
            progress.onProgress(current, total);
        }
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

    private record VehicleIndex(Map<String, Vehicle> byIdentity, Map<UUID, Vehicle> byId) {

        static VehicleIndex from(List<Vehicle> vehicles) {
            Map<String, Vehicle> byIdentity = new HashMap<>();
            Map<UUID, Vehicle> byId = new HashMap<>();
            for (Vehicle vehicle : vehicles) {
                byId.put(vehicle.getId(), vehicle);
                byIdentity.putIfAbsent(identityKey(vehicle), vehicle);
            }
            return new VehicleIndex(byIdentity, byId);
        }

        Optional<Vehicle> findByIdentity(String make, String model, String derivative) {
            String key = VehicleIdentity.normalize(make) + "|"
                    + VehicleIdentity.normalize(model) + "|"
                    + VehicleIdentity.normalize(derivative);
            return Optional.ofNullable(byIdentity.get(key));
        }

        Optional<Vehicle> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }
    }
}
