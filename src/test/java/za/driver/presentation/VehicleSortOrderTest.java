package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import za.driver.model.Pricing;
import za.driver.model.Vehicle;

class VehicleSortOrderTest {

    @Test
    void sortsByMakeThenModelThenPrice() {
        Vehicle cheap = vehicle("BMW", "3 Series", "320i", "500000");
        Vehicle expensive = vehicle("BMW", "3 Series", "330i", "700000");
        Vehicle otherMake = vehicle("Audi", "A4", "35 TFSI", "600000");

        List<Vehicle> vehicles = new ArrayList<>(List.of(expensive, otherMake, cheap));
        vehicles.sort(VehicleSortOrder.byMakeModelPrice());

        assertEquals(List.of(otherMake, cheap, expensive), vehicles);
    }

    @Test
    void nullPricesSortAfterPricedVehiclesWithinSameMakeModel() {
        Vehicle priced = vehicle("Toyota", "Corolla", "XR", "400000");
        Vehicle unpriced = vehicle("Toyota", "Corolla", "Quest", null);

        List<Vehicle> vehicles = new ArrayList<>(List.of(unpriced, priced));
        vehicles.sort(VehicleSortOrder.byMakeModelPrice());

        assertEquals(List.of(priced, unpriced), vehicles);
    }

    private static Vehicle vehicle(String make, String model, String derivative, String price) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setDerivative(derivative);
        if (price != null) {
            Pricing pricing = new Pricing();
            pricing.setPriceZar(new BigDecimal(price));
            vehicle.setPricing(pricing);
        }
        return vehicle;
    }
}
