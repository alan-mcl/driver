package za.driver.import_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;

class ImportValidatorTest {

    private ImportValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImportValidator();
    }

    @Test
    void validMinimalVehicle_passes() {
        VehicleImport vehicleImport = minimalImport();
        assertTrue(validator.validate(vehicleImport, entriesFor(vehicleImport)).isEmpty());
    }

    @Test
    void missingMake_fails() {
        VehicleImport vehicleImport = minimalImport();
        vehicleImport.getVehicle().setMake(null);

        List<String> errors = validator.validate(vehicleImport, entriesFor(vehicleImport));

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("make"));
    }

    @Test
    void missingId_fails() {
        VehicleImport vehicleImport = minimalImport();
        vehicleImport.getVehicle().setId(null);

        List<String> errors = validator.validate(vehicleImport, entriesFor(vehicleImport));

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("id"));
    }

    @Test
    void unsupportedSchemaVersion_fails() {
        VehicleImport vehicleImport = minimalImport();
        vehicleImport.setSchemaVersion(99);

        List<String> errors = validator.validate(vehicleImport, entriesFor(vehicleImport));

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Unsupported schema version"));
    }

    private static List<ImportVehicleEntry> entriesFor(VehicleImport vehicleImport) {
        return List.of(new ImportVehicleEntry(vehicleImport.getVehicle(), Map.of()));
    }

    private static VehicleImport minimalImport() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setStatus(VehicleStatus.CANDIDATE);

        VehicleImport vehicleImport = new VehicleImport();
        vehicleImport.setSchemaVersion(1);
        vehicleImport.setVehicle(vehicle);
        return vehicleImport;
    }
}
