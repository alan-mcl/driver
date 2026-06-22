package za.driver.spreadsheet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import za.driver.model.Vehicle;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringOverrides;

public final class SpreadsheetImportService {

    private final CsvSpreadsheetReader reader;
    private final VehicleRepository vehicleRepository;

    public SpreadsheetImportService(VehicleRepository vehicleRepository) {
        this(new CsvSpreadsheetReader(), vehicleRepository);
    }

    SpreadsheetImportService(CsvSpreadsheetReader reader, VehicleRepository vehicleRepository) {
        this.reader = reader;
        this.vehicleRepository = vehicleRepository;
    }

    public SpreadsheetImportResult preview(Path file) throws IOException {
        SpreadsheetDataSheet sheet = reader.readDataSheet(file);
        SpreadsheetImportResult.Builder builder = new SpreadsheetImportResult.Builder();

        for (int index = 0; index < sheet.rows().size(); index++) {
            Map<String, String> row = sheet.rows().get(index);
            int rowNumber = index + 2;
            if (!VehicleSpreadsheetMapper.hasImportableData(row)) {
                continue;
            }

            String idValue = row.getOrDefault("id", "").trim();
            if (idValue.isEmpty()) {
                builder.addError("Row " + rowNumber + ": id is required");
                continue;
            }

            UUID vehicleId;
            try {
                vehicleId = UUID.fromString(idValue);
            } catch (IllegalArgumentException ex) {
                builder.addError("Row " + rowNumber + ": invalid id '" + idValue + "'");
                continue;
            }

            Optional<Vehicle> existing = vehicleRepository.findById(vehicleId);
            if (existing.isEmpty()) {
                builder.addError("Row " + rowNumber + ": unknown vehicle id " + vehicleId);
                continue;
            }

            Vehicle partial;
            try {
                partial = VehicleSpreadsheetMapper.fromRowMap(row, false);
            } catch (IllegalArgumentException ex) {
                builder.addError("Row " + rowNumber + ": " + ex.getMessage());
                continue;
            }

            partial.setId(vehicleId);
            int changedFields = countChangedFields(existing.get(), partial);
            ScoringOverrides scoringOverrides = VehicleSpreadsheetMapper.scoringOverridesFromRow(row);
            builder.addEntry(new SpreadsheetImportResult.SpreadsheetImportEntry(
                    vehicleId, partial, scoringOverrides, changedFields));
        }

        return builder.build();
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
