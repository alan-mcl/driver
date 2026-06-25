package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import za.driver.model.BrandReliabilityConfig;

public class BrandReliabilityConfigRepository {

    private static final String CONFIG_FILE = "brand-reliability-config.json";

    private final Path configFile;
    private final JsonStore store;

    public BrandReliabilityConfigRepository() {
        this(Paths.get("data"));
    }

    public BrandReliabilityConfigRepository(Path dataRoot) {
        this(dataRoot.resolve(CONFIG_FILE), new JsonStore());
    }

    BrandReliabilityConfigRepository(Path configFile, JsonStore store) {
        this.configFile = configFile;
        this.store = store;
    }

    public BrandReliabilityConfig load() throws IOException {
        if (!Files.exists(configFile)) {
            return BrandReliabilityConfig.empty();
        }
        return store.read(configFile, BrandReliabilityConfig.class);
    }

    public void save(BrandReliabilityConfig config) throws IOException {
        store.write(configFile, config);
    }
}
