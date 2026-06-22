package za.driver.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ScatterPlotStatisticsTest {

    @Test
    void median_oddCount_returnsMiddleValue() {
        assertEquals(3.0, ScatterPlotStatistics.median(List.of(1.0, 3.0, 5.0)));
    }

    @Test
    void median_evenCount_returnsAverageOfMiddlePair() {
        assertEquals(2.5, ScatterPlotStatistics.median(List.of(1.0, 2.0, 3.0, 4.0)));
    }

    @Test
    void linearFit_perfectLine_returnsSlopeAndIntercept() {
        List<ScatterPlotPoint> points = List.of(
                point(0, 1),
                point(2, 5),
                point(4, 9));

        ScatterPlotStatistics.LinearFit fit = ScatterPlotStatistics.linearFit(points).orElseThrow();
        assertEquals(2.0, fit.slope(), 0.0001);
        assertEquals(1.0, fit.intercept(), 0.0001);
    }

    @Test
    void linearFit_verticalSpread_returnsEmpty() {
        List<ScatterPlotPoint> points = List.of(point(2, 1), point(2, 9));
        Optional<ScatterPlotStatistics.LinearFit> fit = ScatterPlotStatistics.linearFit(points);
        assertTrue(fit.isEmpty());
    }

    @Test
    void linearFit_singlePoint_returnsEmpty() {
        assertTrue(ScatterPlotStatistics.linearFit(List.of(point(1, 2))).isEmpty());
    }

    private static ScatterPlotPoint point(double x, double y) {
        return new ScatterPlotPoint(null, x, y, Color.BLACK, "", "", 0, 0);
    }
}
