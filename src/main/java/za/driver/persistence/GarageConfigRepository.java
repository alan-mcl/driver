package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import za.driver.model.GarageDimensions;

public class GarageConfigRepository {

    private static final String CONFIG_FILE = "garage-config.json";

    private final Path configFile;
    private final JsonStore store;

    public GarageConfigRepository() {
        this(Paths.get("data"));
    }

    public GarageConfigRepository(Path dataRoot) {
        this(dataRoot.resolve(CONFIG_FILE), new JsonStore());
    }

    GarageConfigRepository(Path configFile, JsonStore store) {
        this.configFile = configFile;
        this.store = store;
    }

    public GarageDimensions load() throws IOException {
        if (!Files.exists(configFile)) {
            return GarageDimensions.defaults();
        }
        return store.read(configFile, GarageDimensions.class);
    }

    public void save(GarageDimensions dimensions) throws IOException {
        store.write(configFile, dimensions);
    }
}
