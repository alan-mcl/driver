package za.driver.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.AppConfig;

class AppConfigRepositoryTest {

    @TempDir
    Path tempDir;

    private AppConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AppConfigRepository(tempDir);
    }

    @Test
    void load_missingFile_returnsEmptyConfig() throws IOException {
        AppConfig config = repository.load();

        assertNull(config.getActiveProfileId());
    }

    @Test
    void saveAndLoad_roundTripsActiveProfileId() throws IOException {
        UUID profileId = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        AppConfig original = new AppConfig();
        original.setActiveProfileId(profileId);

        repository.save(original);
        AppConfig loaded = repository.load();

        assertEquals(profileId, loaded.getActiveProfileId());
    }
}
