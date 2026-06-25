package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ReliabilityScoreBlenderTest {

    @Test
    void blend_bothPresent_averagesFiftyFifty() {
        assertEquals(90.0, ReliabilityScoreBlender.blend(91.0, 88.0));
    }

    @Test
    void blend_heuristicOnly_returnsHeuristic() {
        assertEquals(91.0, ReliabilityScoreBlender.blend(91.0, null));
    }

    @Test
    void blend_manualEstimateOnly_returnsEstimate() {
        assertEquals(80.0, ReliabilityScoreBlender.blend(null, 80.0));
    }

    @Test
    void blend_neitherPresent_returnsNull() {
        assertNull(ReliabilityScoreBlender.blend(null, null));
    }
}
