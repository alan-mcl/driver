package za.driver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import za.driver.model.Metric;
import za.driver.model.SortColumnRef;
import za.driver.model.VehicleTableColumn;

class VehicleTableColumnResolverTest {

    @Test
    void sortColumnAt_mapsFixedColumns() {
        List<Metric> topMetrics = List.of(Metric.SAFETY, Metric.COMFORT);

        assertEquals(
                new SortColumnRef(VehicleTableColumn.LIST_PRICE, null),
                VehicleTableColumnResolver.sortColumnAt(3, topMetrics).orElseThrow());
        assertEquals(
                new SortColumnRef(VehicleTableColumn.OVERALL_SCORE, null),
                VehicleTableColumnResolver.sortColumnAt(5, topMetrics).orElseThrow());
    }

    @Test
    void sortColumnAt_mapsMetricColumns() {
        List<Metric> topMetrics = List.of(Metric.SAFETY, Metric.COMFORT);

        assertEquals(
                new SortColumnRef(VehicleTableColumn.METRIC, Metric.COMFORT),
                VehicleTableColumnResolver.sortColumnAt(7, topMetrics).orElseThrow());
    }

    @Test
    void modelIndexFor_roundTripsFixedAndMetricColumns() {
        List<Metric> topMetrics = List.of(Metric.SAFETY, Metric.COMFORT);

        assertEquals(
                3,
                VehicleTableColumnResolver.modelIndexFor(
                        new SortColumnRef(VehicleTableColumn.LIST_PRICE, null),
                        topMetrics).orElseThrow());
        assertEquals(
                7,
                VehicleTableColumnResolver.modelIndexFor(
                        new SortColumnRef(VehicleTableColumn.METRIC, Metric.COMFORT),
                        topMetrics).orElseThrow());
    }

    @Test
    void modelIndexFor_absentMetric_returnsEmpty() {
        List<Metric> topMetrics = List.of(Metric.SAFETY);

        Optional<Integer> index = VehicleTableColumnResolver.modelIndexFor(
                new SortColumnRef(VehicleTableColumn.METRIC, Metric.COMFORT),
                topMetrics);

        assertTrue(index.isEmpty());
    }
}
