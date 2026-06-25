package za.driver.service;

import java.io.IOException;
import java.util.Map;

import za.driver.model.BrandReliabilityConfig;
import za.driver.model.BrandReliabilityEntry;
import za.driver.model.ScoringProfile;
import za.driver.persistence.BrandReliabilityConfigRepository;
import za.driver.scoring.BrandReliabilityLookup;

public class BrandReliabilityConfigService {

    private final BrandReliabilityConfigRepository repository;
    private final BrandReliabilityConfig bundledDefaults;
    private BrandReliabilityConfig userConfig;
    private BrandReliabilityLookup mergedLookup;

    public BrandReliabilityConfigService(BrandReliabilityConfigRepository repository) throws IOException {
        this.repository = repository;
        this.bundledDefaults = BrandReliabilityLookup.loadBundledConfig();
        this.userConfig = repository.load();
        refreshMergedLookup();
    }

    public BrandReliabilityLookup getMergedLookup() {
        return mergedLookup;
    }

    public BrandReliabilityConfig getEditableConfig() {
        return BrandReliabilityLookup.overlayConfigs(bundledDefaults, userConfig);
    }

    public void save(BrandReliabilityConfig editedConfig) throws IOException {
        validate(editedConfig);
        BrandReliabilityConfig toSave = new BrandReliabilityConfig();
        toSave.setSchemaVersion(1);
        toSave.setBrands(editedConfig.getBrands());
        toSave.setAliases(editedConfig.getAliases());
        repository.save(toSave);
        userConfig = toSave;
        refreshMergedLookup();
    }

    public void saveAndRecalculateAll(
            BrandReliabilityConfig editedConfig,
            ScoringProfile profile,
            ScoringProfileService profileService) throws IOException {
        save(editedConfig);
        profileService.recalculateAllVehicles(profile);
    }

    private void refreshMergedLookup() {
        mergedLookup = BrandReliabilityLookup.merge(bundledDefaults, userConfig);
    }

    public void validate(BrandReliabilityConfig config) {
        if (config == null || config.getBrands() == null) {
            throw new IllegalArgumentException("At least one brand entry is required.");
        }
        for (Map.Entry<String, BrandReliabilityEntry> entry : config.getBrands().entrySet()) {
            String brandName = entry.getKey();
            if (brandName == null || brandName.isBlank()) {
                throw new IllegalArgumentException("Brand name must not be blank.");
            }
            BrandReliabilityEntry values = entry.getValue();
            if (values == null) {
                throw new IllegalArgumentException("Brand entry must not be null: " + brandName);
            }
            validateScore("Reliability", brandName, values.getReliability());
            validateScore("Confidence", brandName, values.getConfidence());
        }
    }

    private static void validateScore(String label, String brandName, int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(label + " for " + brandName + " must be between 0 and 100.");
        }
    }
}
