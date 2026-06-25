package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import za.driver.model.AppConfig;

public class AppConfigRepository {

    private static final String CONFIG_FILE = "app-config.json";

    private final Path configFile;
    private final JsonStore store;

    public AppConfigRepository() {
        this(Paths.get("data"));
    }

    public AppConfigRepository(Path dataRoot) {
        this(dataRoot.resolve(CONFIG_FILE), new JsonStore());
    }

    AppConfigRepository(Path configFile, JsonStore store) {
        this.configFile = configFile;
        this.store = store;
    }

    public AppConfig load() throws IOException {
        if (!Files.exists(configFile)) {
            return new AppConfig();
        }
        return store.read(configFile, AppConfig.class);
    }

    public void save(AppConfig config) throws IOException {
        store.write(configFile, config);
    }
}
