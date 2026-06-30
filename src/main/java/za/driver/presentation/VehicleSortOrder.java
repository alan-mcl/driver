package za.driver.presentation;

import java.math.BigDecimal;
import java.util.Comparator;

import za.driver.model.Pricing;
import za.driver.model.Vehicle;

public final class VehicleSortOrder {

    private VehicleSortOrder() {
    }

    public static Comparator<Vehicle> byMakeModelPrice() {
        return Comparator
                .comparing(Vehicle::getMake, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Vehicle::getModel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(VehicleSortOrder::priceZar, Comparator.nullsLast(BigDecimal::compareTo));
    }

    private static BigDecimal priceZar(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        Pricing pricing = vehicle.getPricing();
        return pricing != null ? pricing.getPriceZar() : null;
    }
}
