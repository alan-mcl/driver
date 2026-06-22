package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ScoreUtilPiecewiseTest {

    @Test
    void piecewiseLinear_belowRange_clampsToFirstScore() {
        Double score = ScoreUtil.piecewiseLinear(40, new double[] {50, 80, 120}, new double[] {0, 50, 100});
        assertEquals(0.0, score, 0.01);
    }

    @Test
    void piecewiseLinear_aboveRange_clampsToLastScore() {
        Double score = ScoreUtil.piecewiseLinear(130, new double[] {50, 80, 120}, new double[] {0, 50, 100});
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void piecewiseLinear_atBreakpoint_returnsExactScore() {
        Double score = ScoreUtil.piecewiseLinear(80, new double[] {50, 80, 120}, new double[] {0, 50, 100});
        assertEquals(50.0, score, 0.01);
    }

    @Test
    void piecewiseLinear_midSegment_interpolates() {
        Double score = ScoreUtil.piecewiseLinear(65, new double[] {50, 80, 120}, new double[] {0, 50, 100});
        assertEquals(25.0, score, 0.01);
    }

    @Test
    void piecewiseLinear_invalidInput_returnsNull() {
        assertNull(ScoreUtil.piecewiseLinear(80, new double[] {50, 80}, new double[] {0, 50, 100}));
    }
}
