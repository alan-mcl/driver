package za.driver.import_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import za.driver.model.DataQuality;
import za.driver.model.Vehicle;

class VehicleImportMergerTest {

    @Test
    void migrateLegacyPricing_migratesPriceZarDataQualityKey() {
        Vehicle vehicle = new Vehicle();
        vehicle.setDataQuality(Map.of("pricing.priceZar", DataQuality.VERIFIED));

        VehicleImportMerger.migrateLegacyPricing(vehicle);

        assertEquals(DataQuality.VERIFIED, vehicle.getDataQuality().get("pricing.listPrice"));
        assertNull(vehicle.getDataQuality().get("pricing.priceZar"));
    }

    @Test
    void migrateLegacyPricing_migratesListPriceZarDataQualityKey() {
        Vehicle vehicle = new Vehicle();
        vehicle.setDataQuality(Map.of("pricing.listPriceZar", DataQuality.VERIFIED));

        VehicleImportMerger.migrateLegacyPricing(vehicle);

        assertEquals(DataQuality.VERIFIED, vehicle.getDataQuality().get("pricing.listPrice"));
        assertNull(vehicle.getDataQuality().get("pricing.listPriceZar"));
    }

    @Test
    void migrateLegacyPricing_migratesDealerOfferZarDataQualityKey() {
        Vehicle vehicle = new Vehicle();
        vehicle.setDataQuality(Map.of("pricing.dealerOfferZar", DataQuality.ESTIMATED));

        VehicleImportMerger.migrateLegacyPricing(vehicle);

        assertEquals(DataQuality.ESTIMATED, vehicle.getDataQuality().get("pricing.dealerOffer"));
        assertNull(vehicle.getDataQuality().get("pricing.dealerOfferZar"));
    }
}
