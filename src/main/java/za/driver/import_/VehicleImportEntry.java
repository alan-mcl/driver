package za.driver.import_;

import java.util.Map;

import za.driver.model.DataQuality;
import za.driver.model.Vehicle;

public class VehicleImportEntry {

    private Vehicle vehicle;
    private Map<String, DataQuality> dataQuality;

    public VehicleImportEntry() {
    }

    public VehicleImportEntry(Vehicle vehicle, Map<String, DataQuality> dataQuality) {
        this.vehicle = vehicle;
        this.dataQuality = dataQuality;
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
}
