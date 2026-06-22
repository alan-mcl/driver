package za.driver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import za.driver.scoring.ScoringTestFixtures;

class VehicleIdentityTest {

    @Test
    void label_includesMakeModelDerivative() {
        assertEquals("Toyota Corolla 1.8 XS", VehicleIdentity.label(ScoringTestFixtures.fullVehicle()));
    }

    @Test
    void shortLabel_omitsMake() {
        assertEquals("Corolla 1.8 XS", VehicleIdentity.shortLabel(ScoringTestFixtures.fullVehicle()));
    }
}
