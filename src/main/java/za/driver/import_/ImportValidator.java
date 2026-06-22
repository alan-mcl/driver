package za.driver.import_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;

public class ImportValidator {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    public List<String> validate(VehicleImport vehicleImport, List<ImportVehicleEntry> entries) {
        List<String> errors = new ArrayList<>();

        if (vehicleImport == null) {
            errors.add("Import object is required");
            return errors;
        }

        if (vehicleImport.getSchemaVersion() != SUPPORTED_SCHEMA_VERSION) {
            errors.add("Unsupported schema version: " + vehicleImport.getSchemaVersion());
        }

        if (entries == null || entries.isEmpty()) {
            errors.add("At least one vehicle is required");
            return errors;
        }

        for (int i = 0; i < entries.size(); i++) {
            String prefix = entryPrefix(i, entries.size());
            validateVehicle(entries.get(i).getVehicle(), prefix, errors);
        }

        validateDuplicateIdentities(entries, errors);
        return errors;
    }

    private static void validateVehicle(Vehicle vehicle, String prefix, List<String> errors) {
        if (vehicle == null) {
            errors.add(prefix + "Vehicle object is required");
            return;
        }
        if (vehicle.getId() == null) {
            errors.add(prefix + "Vehicle id is required");
        }
        if (vehicle.getMake() == null || vehicle.getMake().isBlank()) {
            errors.add(prefix + "Vehicle make is required");
        }
        if (vehicle.getModel() == null || vehicle.getModel().isBlank()) {
            errors.add(prefix + "Vehicle model is required");
        }
        if (vehicle.getStatus() == null) {
            errors.add(prefix + "Vehicle status is required");
        }
    }

    private static void validateDuplicateIdentities(List<ImportVehicleEntry> entries, List<String> errors) {
        Map<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < entries.size(); i++) {
            Vehicle vehicle = entries.get(i).getVehicle();
            if (vehicle == null) {
                continue;
            }
            String key = identityKey(vehicle);
            Integer firstIndex = seen.putIfAbsent(key, i);
            if (firstIndex != null) {
                errors.add(entryPrefix(i, entries.size()) + "Duplicate identity matches "
                        + entryPrefix(firstIndex, entries.size()).trim());
            }
        }
    }

    private static String identityKey(Vehicle vehicle) {
        return VehicleIdentity.normalize(vehicle.getMake()) + "|"
                + VehicleIdentity.normalize(vehicle.getModel()) + "|"
                + VehicleIdentity.normalize(vehicle.getDerivative());
    }

    private static String entryPrefix(int index, int total) {
        if (total == 1) {
            return "";
        }
        return "vehicles[" + index + "]: ";
    }
}
