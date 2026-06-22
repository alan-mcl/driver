package za.driver.import_;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.driver.model.DataQuality;
import za.driver.model.Vehicle;
import za.driver.persistence.JsonStore;

public class ImportService {

    private final ObjectMapper mapper;
    private final ImportValidator validator;

    public ImportService() {
        this(JsonStore.createMapper(), new ImportValidator());
    }

    ImportService(ObjectMapper mapper, ImportValidator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public ImportResult parse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            VehicleImport vehicleImport = mapper.treeToValue(root, VehicleImport.class);
            if (root.has("vehicles") && root.get("vehicles").isArray()) {
                vehicleImport.setVehicles(parseVehicleEntries(root.get("vehicles")));
            }
            return buildResult(vehicleImport);
        } catch (JsonProcessingException e) {
            return ImportResult.failure(List.of("Invalid JSON: " + e.getOriginalMessage()));
        }
    }

    public ImportResult parse(Path file) throws IOException {
        String json = Files.readString(file);
        return parse(json);
    }

    public ImportResult preview(String json) {
        return parse(json);
    }

    private ImportResult buildResult(VehicleImport vehicleImport) {
        List<ImportVehicleEntry> entries = resolveEntries(vehicleImport);
        List<String> errors = validator.validate(vehicleImport, entries);
        if (errors.isEmpty()) {
            return ImportResult.success(entries);
        }
        return ImportResult.failure(errors, entries);
    }

    private static List<ImportVehicleEntry> resolveEntries(VehicleImport vehicleImport) {
        if (vehicleImport == null) {
            return List.of();
        }

        if (vehicleImport.getVehicles() != null && !vehicleImport.getVehicles().isEmpty()) {
            List<ImportVehicleEntry> entries = new ArrayList<>();
            for (VehicleImportEntry entry : vehicleImport.getVehicles()) {
                Map<String, DataQuality> dataQuality = entry == null || entry.getDataQuality() == null
                        ? Map.of()
                        : new HashMap<>(entry.getDataQuality());
                Vehicle vehicle = entry == null ? null : entry.getVehicle();
                entries.add(new ImportVehicleEntry(vehicle, dataQuality));
            }
            return entries;
        }

        if (vehicleImport.getVehicle() != null) {
            Map<String, DataQuality> dataQuality = vehicleImport.getDataQuality() == null
                    ? Map.of()
                    : new HashMap<>(vehicleImport.getDataQuality());
            return List.of(new ImportVehicleEntry(vehicleImport.getVehicle(), dataQuality));
        }

        return List.of();
    }

    private List<VehicleImportEntry> parseVehicleEntries(JsonNode vehiclesNode) throws JsonProcessingException {
        List<VehicleImportEntry> entries = new ArrayList<>();
        for (JsonNode node : vehiclesNode) {
            if (node.has("vehicle")) {
                entries.add(mapper.treeToValue(node, VehicleImportEntry.class));
            } else {
                Vehicle vehicle = mapper.treeToValue(node, Vehicle.class);
                Map<String, DataQuality> dataQuality = node.has("dataQuality")
                        ? mapper.treeToValue(node.get("dataQuality"),
                                mapper.getTypeFactory().constructMapType(Map.class, String.class, DataQuality.class))
                        : Map.of();
                entries.add(new VehicleImportEntry(vehicle, dataQuality));
            }
        }
        return entries;
    }
}
