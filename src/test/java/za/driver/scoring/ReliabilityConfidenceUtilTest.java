package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import za.driver.model.Vehicle;

class ReliabilityConfidenceUtilTest {

    @Test
    void resolve_withoutPersistedConfidence_usesBrandLookup() {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();

        assertEquals(98, ReliabilityConfidenceUtil.resolve(vehicle, null));
        assertEquals("98 (High)", ReliabilityConfidenceUtil.format(vehicle, null));
    }
}
