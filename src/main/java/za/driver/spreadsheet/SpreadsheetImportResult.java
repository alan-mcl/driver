package za.driver.spreadsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import za.driver.model.Vehicle;
import za.driver.scoring.ScoringOverrides;

public final class SpreadsheetImportResult {

    private final List<SpreadsheetImportEntry> entries;
    private final List<String> errors;

    private SpreadsheetImportResult(List<SpreadsheetImportEntry> entries, List<String> errors) {
        this.entries = List.copyOf(entries);
        this.errors = List.copyOf(errors);
    }

    public static SpreadsheetImportResult success(List<SpreadsheetImportEntry> entries) {
        return new SpreadsheetImportResult(entries, List.of());
    }

    public static SpreadsheetImportResult failure(List<String> errors) {
        return new SpreadsheetImportResult(List.of(), errors);
    }

    public static SpreadsheetImportResult failure(List<String> errors, List<SpreadsheetImportEntry> entries) {
        return new SpreadsheetImportResult(entries, errors);
    }

    public boolean isValid() {
        return errors.isEmpty() && !entries.isEmpty();
    }

    public List<SpreadsheetImportEntry> getEntries() {
        return entries;
    }

    public List<String> getErrors() {
        return errors;
    }

    public int updateCount() {
        return entries.size();
    }

    public record SpreadsheetImportEntry(
            UUID vehicleId,
            Vehicle vehicle,
            ScoringOverrides scoringOverrides,
            int changedFieldCount) {
    }

    public static final class Builder {
        private final List<SpreadsheetImportEntry> entries = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        public void addEntry(SpreadsheetImportEntry entry) {
            entries.add(entry);
        }

        public void addError(String error) {
            errors.add(error);
        }

        public SpreadsheetImportResult build() {
            if (errors.isEmpty() && entries.isEmpty()) {
                return failure(List.of("No importable rows found in Data sheet"));
            }
            if (!errors.isEmpty()) {
                return failure(errors, entries);
            }
            return success(entries);
        }
    }
}
