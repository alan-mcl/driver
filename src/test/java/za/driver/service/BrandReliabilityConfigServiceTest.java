package za.driver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.BrandReliabilityConfig;
import za.driver.model.BrandReliabilityEntry;
import za.driver.persistence.BrandReliabilityConfigRepository;
import za.driver.scoring.BrandReliabilityLookup;

class BrandReliabilityConfigServiceTest {

    @TempDir
    Path tempDir;

    private BrandReliabilityConfigService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new BrandReliabilityConfigService(new BrandReliabilityConfigRepository(tempDir));
    }

    @Test
    void getMergedLookup_withoutUserConfig_usesBundledDefaults() {
        assertEquals(95, service.getMergedLookup().reliabilityScore("Toyota"));
    }

    @Test
    void save_overridesBundledBrand() throws IOException {
        BrandReliabilityConfig config = new BrandReliabilityConfig();
        config.getBrands().put("Toyota", new BrandReliabilityEntry(99, 98));

        service.save(config);

        assertEquals(99, service.getMergedLookup().reliabilityScore("Toyota"));
    }

    @Test
    void save_addsNewBrand() throws IOException {
        BrandReliabilityConfig config = new BrandReliabilityConfig();
        config.getBrands().put("Volvo", new BrandReliabilityEntry(85, 80));

        service.save(config);

        assertEquals(85, service.getMergedLookup().reliabilityScore("Volvo"));
    }

    @Test
    void getMergedLookup_unknownBrand_returnsNull() {
        assertNull(service.getMergedLookup().reliabilityScore("UnknownBrand"));
    }

    @Test
    void overlayConfigs_userValuesOverrideBundled() {
        BrandReliabilityConfig bundled = BrandReliabilityLookup.loadBundledConfig();
        BrandReliabilityConfig user = BrandReliabilityConfig.empty();
        user.getBrands().put("Toyota", new BrandReliabilityEntry(70, 60));

        BrandReliabilityConfig merged = BrandReliabilityLookup.overlayConfigs(bundled, user);
        BrandReliabilityLookup lookup = BrandReliabilityLookup.fromConfig(merged);

        assertEquals(70, lookup.reliabilityScore("Toyota"));
        assertEquals(93, lookup.reliabilityScore("Honda"));
    }
}
