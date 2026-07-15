package za.driver.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.AppConfig;
import za.driver.model.BodyType;
import za.driver.model.CurrencyPreset;
import za.driver.model.DisplayPreferences;
import za.driver.model.Metric;
import za.driver.model.VehicleFilterPreferences;
import za.driver.model.VehicleListPreferences;
import za.driver.model.VehicleSortPreferences;
import za.driver.model.VehicleStatus;
import za.driver.model.VehicleTableColumn;

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

    @Test
    void saveAndLoad_roundTripsVehicleListPreferences() throws IOException {
        AppConfig original = new AppConfig();
        VehicleListPreferences vehicleList = new VehicleListPreferences();
        VehicleFilterPreferences filter = new VehicleFilterPreferences();
        filter.setMaxPrice(BigDecimal.valueOf(1_500_000));
        filter.setBodyType(BodyType.SUV);
        filter.setStatus(VehicleStatus.CANDIDATE);
        filter.setMinOverallScore(70.0);
        filter.setMinGarageClearanceMm(500);
        vehicleList.setFilter(filter);

        VehicleSortPreferences sort = new VehicleSortPreferences();
        sort.setColumnKey(VehicleTableColumn.OVERALL_SCORE);
        sort.setAscending(false);
        vehicleList.setSort(sort);
        original.setVehicleList(vehicleList);

        repository.save(original);
        AppConfig loaded = repository.load();

        assertEquals(BigDecimal.valueOf(1_500_000), loaded.getVehicleList().getFilter().getMaxPrice());
        assertEquals(BodyType.SUV, loaded.getVehicleList().getFilter().getBodyType());
        assertEquals(VehicleStatus.CANDIDATE, loaded.getVehicleList().getFilter().getStatus());
        assertEquals(70.0, loaded.getVehicleList().getFilter().getMinOverallScore());
        assertEquals(500, loaded.getVehicleList().getFilter().getMinGarageClearanceMm());
        assertEquals(VehicleTableColumn.OVERALL_SCORE, loaded.getVehicleList().getSort().getColumnKey());
        assertEquals(false, loaded.getVehicleList().getSort().getAscending());
    }

    @Test
    void saveAndLoad_roundTripsMetricSortAndDisplayPreferences() throws IOException {
        AppConfig original = new AppConfig();
        VehicleSortPreferences sort = new VehicleSortPreferences();
        sort.setColumnKey(VehicleTableColumn.METRIC);
        sort.setMetric(Metric.SAFETY);
        sort.setAscending(true);
        original.getVehicleList().setSort(sort);

        DisplayPreferences display = new DisplayPreferences();
        display.setPreset(CurrencyPreset.CUSTOM);
        display.setCustomSymbol("CHF");
        display.setCustomLocaleTag("de-CH");
        original.setDisplay(display);

        repository.save(original);
        AppConfig loaded = repository.load();

        assertEquals(VehicleTableColumn.METRIC, loaded.getVehicleList().getSort().getColumnKey());
        assertEquals(Metric.SAFETY, loaded.getVehicleList().getSort().getMetric());
        assertTrue(loaded.getVehicleList().getSort().getAscending());
        assertEquals(CurrencyPreset.CUSTOM, loaded.getDisplay().getPreset());
        assertEquals("CHF", loaded.getDisplay().getCustomSymbol());
        assertEquals("de-CH", loaded.getDisplay().getCustomLocaleTag());
    }

    @Test
    void load_legacyMaxPriceZar_deserializesAsMaxPrice() throws IOException {
        java.nio.file.Files.writeString(
                tempDir.resolve("app-config.json"),
                """
                        {
                          "vehicleList": {
                            "filter": {
                              "maxPriceZar": 1500000
                            }
                          }
                        }
                        """);

        AppConfig loaded = repository.load();

        assertEquals(BigDecimal.valueOf(1_500_000), loaded.getVehicleList().getFilter().getMaxPrice());
    }
}
