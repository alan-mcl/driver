package za.driver.service;

import java.io.IOException;

import za.driver.model.GarageDimensions;
import za.driver.persistence.GarageConfigRepository;

public class GarageConfigService {

    private final GarageConfigRepository repository;
    private GarageDimensions cached;

    public GarageConfigService(GarageConfigRepository repository) throws IOException {
        this.repository = repository;
        this.cached = repository.load();
    }

    public GarageDimensions getGarageDimensions() {
        return cached;
    }

    public void save(GarageDimensions dimensions) throws IOException {
        if (dimensions.garageWidthMm() <= 0
                || dimensions.arcRadiusMm() <= 0
                || dimensions.arcStartHeightMm() <= 0) {
            throw new IllegalArgumentException("All garage dimensions must be greater than zero.");
        }
        repository.save(dimensions);
        cached = dimensions;
    }
}
