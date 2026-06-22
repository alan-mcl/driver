package za.driver.import_;

import java.util.Map;

import za.driver.model.DataQuality;
import za.driver.model.Vehicle;

public class ImportVehicleEntry {

    private final Vehicle vehicle;
    private final Map<String, DataQuality> dataQuality;

    public ImportVehicleEntry(Vehicle vehicle, Map<String, DataQuality> dataQuality) {
        this.vehicle = vehicle;
        this.dataQuality = dataQuality == null ? Map.of() : Map.copyOf(dataQuality);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Map<String, DataQuality> getDataQuality() {
        return dataQuality;
    }
}
