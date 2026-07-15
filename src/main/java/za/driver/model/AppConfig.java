package za.driver.model;

import java.util.UUID;

public class AppConfig {

    private UUID activeProfileId;
    private VehicleListPreferences vehicleList = new VehicleListPreferences();
    private DisplayPreferences display = new DisplayPreferences();

    public UUID getActiveProfileId() {
        return activeProfileId;
    }

    public void setActiveProfileId(UUID activeProfileId) {
        this.activeProfileId = activeProfileId;
    }

    public VehicleListPreferences getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(VehicleListPreferences vehicleList) {
        this.vehicleList = vehicleList != null ? vehicleList : new VehicleListPreferences();
    }

    public DisplayPreferences getDisplay() {
        return display;
    }

    public void setDisplay(DisplayPreferences display) {
        this.display = display != null ? display : new DisplayPreferences();
    }
}
