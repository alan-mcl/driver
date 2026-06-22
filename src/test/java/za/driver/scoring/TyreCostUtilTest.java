package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TyreCostUtilTest {

    @Test
    void tyreCostScore_null_returnsNull() {
        assertNull(TyreCostUtil.tyreCostScore(null));
    }

    @Test
    void tyreCostScore_blank_returnsNull() {
        assertNull(TyreCostUtil.tyreCostScore("  "));
    }

    @Test
    void tyreCostScore_unparseable_returnsNull() {
        assertNull(TyreCostUtil.tyreCostScore("invalid"));
    }

    @Test
    void tyreCostScore_r16_returnsHighScore() {
        Double score = TyreCostUtil.tyreCostScore("195/60 R16");
        assertEquals(81.4, score, 0.1);
    }

    @Test
    void tyreCostScore_r18_returnsLowerScore() {
        Double score = TyreCostUtil.tyreCostScore("215/55 R18");
        assertEquals(44.3, score, 0.1);
    }

    @Test
    void tyreCostScore_wideTyre_penalizesWidth() {
        Double narrow = TyreCostUtil.tyreCostScore("195/60 R17");
        Double wide = TyreCostUtil.tyreCostScore("225/40 R17");
        assertEquals(66.4, narrow, 0.1);
        assertEquals(55.7, wide, 0.1);
    }
}
