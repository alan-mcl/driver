package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import za.driver.model.Economy;
import za.driver.model.Ownership;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.model.Wheels;

class RunningCostCalculatorTest {

    private final RunningCostCalculator calculator = new RunningCostCalculator();

    @Test
    void calculate_corollaCrossExample_matchesExpectedScore() {
        Vehicle vehicle = corollaCrossVehicle();

        Double score = calculator.calculate(vehicle);

        assertNotNull(score);
        assertEquals(71.09, score, 0.01);
    }

    @Test
    void calculate_missingPartsSupport_renormalizes() {
        Vehicle vehicle = corollaCrossVehicle();
        vehicle.getOwnership().setPartsSupportScore(null);

        Double score = calculator.calculate(vehicle);

        assertNotNull(score);
        assertEquals(64.94, score, 0.01);
    }

    @Test
    void calculate_noInputs_returnsNull() {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Test");
        vehicle.setModel("Test");
        vehicle.setStatus(VehicleStatus.CANDIDATE);

        assertNull(calculator.calculate(vehicle));
    }

    private static Vehicle corollaCrossVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla Cross");
        vehicle.setStatus(VehicleStatus.CANDIDATE);

        Economy economy = new Economy();
        economy.setFuelConsumptionCombined(6.7);
        vehicle.setEconomy(economy);

        Ownership ownership = new Ownership();
        ownership.setWarrantyYears(3);
        ownership.setWarrantyKm(100_000);
        ownership.setServicePlanKm(90_000);
        ownership.setPartsSupportScore(92);
        ownership.setLocalProduction(true);
        vehicle.setOwnership(ownership);

        Wheels wheels = new Wheels();
        wheels.setTyreSize("215/60 R17");
        vehicle.setWheels(wheels);

        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("420700"));
        pricing.setListPriceDate(LocalDate.of(2026, 6, 18));
        vehicle.setPricing(pricing);

        return vehicle;
    }
}
