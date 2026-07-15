package za.driver.service;

import java.io.IOException;
import java.util.UUID;

import za.driver.model.AppConfig;
import za.driver.model.DisplayPreferences;
import za.driver.model.VehicleListPreferences;
import za.driver.persistence.AppConfigRepository;

public class AppConfigService {

    private final AppConfigRepository repository;
    private AppConfig cached;

    public AppConfigService(AppConfigRepository repository) throws IOException {
        this.repository = repository;
        this.cached = repository.load();
        normalize(cached);
    }

    public UUID getActiveProfileId() {
        return cached.getActiveProfileId();
    }

    public void setActiveProfileId(UUID activeProfileId) throws IOException {
        cached.setActiveProfileId(activeProfileId);
        repository.save(cached);
    }

    public VehicleListPreferences getVehicleListPreferences() {
        return cached.getVehicleList();
    }

    public void setVehicleListPreferences(VehicleListPreferences preferences) throws IOException {
        cached.setVehicleList(preferences);
        repository.save(cached);
    }

    public void setFilterPreferences(za.driver.model.VehicleFilterPreferences filter) throws IOException {
        cached.getVehicleList().setFilter(filter);
        repository.save(cached);
    }

    public void setSortPreferences(za.driver.model.VehicleSortPreferences sort) throws IOException {
        cached.getVehicleList().setSort(sort);
        repository.save(cached);
    }

    public DisplayPreferences getDisplayPreferences() {
        return cached.getDisplay();
    }

    public void setDisplayPreferences(DisplayPreferences display) throws IOException {
        cached.setDisplay(display);
        normalize(cached);
        repository.save(cached);
    }

    private static void normalize(AppConfig config) {
        if (config.getVehicleList() == null) {
            config.setVehicleList(VehicleListPreferencesMapper.defaults());
        }
        if (config.getDisplay() == null) {
            config.setDisplay(new DisplayPreferences());
        }
    }
}
