package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CoverageScoreUtilTest {

    @Test
    void coverageScore_bothNull_returnsNull() {
        assertNull(CoverageScoreUtil.coverageScore(null, null, 7, 200_000));
    }

    @Test
    void coverageScore_yearsOnly_returnsYearsScore() {
        Double score = CoverageScoreUtil.coverageScore(3, null, 7, 200_000);
        assertEquals(42.86, score, 0.01);
    }

    @Test
    void coverageScore_kmOnly_returnsKmScore() {
        Double score = CoverageScoreUtil.coverageScore(null, 90_000, 5, 100_000);
        assertEquals(90.0, score, 0.01);
    }

    @Test
    void coverageScore_bothPresent_returnsAverage() {
        Double score = CoverageScoreUtil.coverageScore(3, 100_000, 7, 200_000);
        assertEquals(46.43, score, 0.01);
    }

    @Test
    void coverageScore_atMax_returns100() {
        Double score = CoverageScoreUtil.coverageScore(7, 200_000, 7, 200_000);
        assertEquals(100.0, score, 0.01);
    }
}
