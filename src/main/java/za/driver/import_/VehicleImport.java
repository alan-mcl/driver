package za.driver.import_;

import java.util.List;
import java.util.Map;

import za.driver.model.DataQuality;
import za.driver.model.Vehicle;

public class VehicleImport {

    private int schemaVersion;
    private Vehicle vehicle;
    private Map<String, DataQuality> dataQuality;
    private List<VehicleImportEntry> vehicles;

    public VehicleImport() {
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Map<String, DataQuality> getDataQuality() {
        return dataQuality;
    }

    public void setDataQuality(Map<String, DataQuality> dataQuality) {
        this.dataQuality = dataQuality;
    }

    public List<VehicleImportEntry> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleImportEntry> vehicles) {
        this.vehicles = vehicles;
    }
}
