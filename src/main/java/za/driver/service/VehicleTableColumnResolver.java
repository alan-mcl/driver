package za.driver.service;

import java.util.List;
import java.util.Optional;

import za.driver.model.Metric;
import za.driver.model.SortColumnRef;
import za.driver.model.VehicleTableColumn;

public final class VehicleTableColumnResolver {

    private static final int FIXED_PREFIX_COLUMNS = 6;

    private VehicleTableColumnResolver() {
    }

    public static Optional<SortColumnRef> sortColumnAt(int columnIndex, List<Metric> topMetrics) {
        if (columnIndex < 0) {
            return Optional.empty();
        }
        if (columnIndex == 0) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.MAKE, null));
        }
        if (columnIndex == 1) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.MODEL, null));
        }
        if (columnIndex == 2) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.DERIVATIVE, null));
        }
        if (columnIndex == 3) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.LIST_PRICE, null));
        }
        if (columnIndex == 4) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.DEALER_OFFER, null));
        }
        if (columnIndex == 5) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.OVERALL_SCORE, null));
        }
        int metricEnd = FIXED_PREFIX_COLUMNS + topMetrics.size();
        if (columnIndex < metricEnd) {
            return Optional.of(new SortColumnRef(
                    VehicleTableColumn.METRIC,
                    topMetrics.get(columnIndex - FIXED_PREFIX_COLUMNS)));
        }
        int scorePer100kIndex = metricEnd;
        int dataCompletenessIndex = metricEnd + 1;
        int garageClearanceIndex = metricEnd + 2;
        if (columnIndex == scorePer100kIndex) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.SCORE_PER_100K, null));
        }
        if (columnIndex == dataCompletenessIndex) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.DATA_COMPLETENESS, null));
        }
        if (columnIndex == garageClearanceIndex) {
            return Optional.of(new SortColumnRef(VehicleTableColumn.GARAGE_CLEARANCE, null));
        }
        return Optional.empty();
    }

    public static Optional<Integer> modelIndexFor(SortColumnRef ref, List<Metric> topMetrics) {
        if (ref == null || ref.columnKey() == null) {
            return Optional.empty();
        }
        return switch (ref.columnKey()) {
            case MAKE -> Optional.of(0);
            case MODEL -> Optional.of(1);
            case DERIVATIVE -> Optional.of(2);
            case LIST_PRICE -> Optional.of(3);
            case DEALER_OFFER -> Optional.of(4);
            case OVERALL_SCORE -> Optional.of(5);
            case METRIC -> {
                if (ref.metric() == null) {
                    yield Optional.empty();
                }
                int index = topMetrics.indexOf(ref.metric());
                if (index < 0) {
                    yield Optional.empty();
                }
                yield Optional.of(FIXED_PREFIX_COLUMNS + index);
            }
            case SCORE_PER_100K -> Optional.of(FIXED_PREFIX_COLUMNS + topMetrics.size());
            case DATA_COMPLETENESS -> Optional.of(FIXED_PREFIX_COLUMNS + topMetrics.size() + 1);
            case GARAGE_CLEARANCE -> Optional.of(FIXED_PREFIX_COLUMNS + topMetrics.size() + 2);
        };
    }
}
