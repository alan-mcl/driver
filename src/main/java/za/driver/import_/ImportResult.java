package za.driver.import_;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.driver.model.DataQuality;
import za.driver.model.Vehicle;

public class ImportResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<ImportVehicleEntry> entries;

    private ImportResult(boolean valid, List<String> errors, List<ImportVehicleEntry> entries) {
        this.valid = valid;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static ImportResult success(List<ImportVehicleEntry> entries) {
        return new ImportResult(true, List.of(), entries);
    }

    public static ImportResult success(Vehicle vehicle, Map<String, DataQuality> dataQuality) {
        return success(List.of(new ImportVehicleEntry(vehicle, dataQuality)));
    }

    public static ImportResult failure(List<String> errors) {
        return new ImportResult(false, errors, List.of());
    }

    public static ImportResult failure(List<String> errors, List<ImportVehicleEntry> entries) {
        return new ImportResult(false, errors, entries);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<ImportVehicleEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getVehicleCount() {
        return entries.size();
    }

    public Vehicle getVehicle() {
        return entries.isEmpty() ? null : entries.get(0).getVehicle();
    }

    public Map<String, DataQuality> getDataQuality() {
        return entries.isEmpty() ? Map.of() : entries.get(0).getDataQuality();
    }
}
