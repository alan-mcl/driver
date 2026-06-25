package za.driver.service;

import java.io.IOException;
import java.util.UUID;

import za.driver.model.AppConfig;
import za.driver.persistence.AppConfigRepository;

public class AppConfigService {

    private final AppConfigRepository repository;
    private AppConfig cached;

    public AppConfigService(AppConfigRepository repository) throws IOException {
        this.repository = repository;
        this.cached = repository.load();
    }

    public UUID getActiveProfileId() {
        return cached.getActiveProfileId();
    }

    public void setActiveProfileId(UUID activeProfileId) throws IOException {
        cached.setActiveProfileId(activeProfileId);
        repository.save(cached);
    }
}
