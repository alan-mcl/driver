package za.driver.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import za.driver.garage.GarageClearanceCalculator;
import za.driver.model.DerivedMetrics;
import za.driver.model.GarageDimensions;
import za.driver.model.Metric;
import za.driver.model.Pricing;
import za.driver.model.ScoringProfile;
import za.driver.model.Vehicle;
import za.driver.presentation.MetricLabels;
import za.driver.scoring.MetricScores;
import za.driver.scoring.ScoringInputCoverage;
import za.driver.scoring.TopWeightedMetrics;
import za.driver.service.VehicleFilter;
import za.driver.service.VehicleFilterCriteria;

public class VehicleListPanel extends JPanel {

    private static final int FIXED_PREFIX_COLUMNS = 5;
    private static final int TOP_METRIC_COUNT = 5;
    private static final int SCORE_PER_100K_COLUMN_OFFSET = 0;
    private static final int DATA_COMPLETENESS_COLUMN_OFFSET = 1;
    private static final int GARAGE_CLEARANCE_COLUMN_OFFSET = 2;

    private final FilterBar filterBar = new FilterBar();
    private final VehicleTableModel tableModel;
    private final JTable table;
    private VehicleFilterCriteria filterCriteria = VehicleFilterCriteria.empty();
    private List<Vehicle> allVehicles = new ArrayList<>();
    private GarageDimensions garageDimensions = GarageDimensions.defaults();

    public VehicleListPanel(ScoringProfile activeProfile, GarageDimensions garageDimensions) {
        super(new java.awt.BorderLayout());
        this.garageDimensions = garageDimensions;
        tableModel = new VehicleTableModel(activeProfile);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(225, 225, 225));
        table.setIntercellSpacing(new Dimension(1, 1));
        configureRenderers();
        configureColumnWidths();
        filterBar.addChangeListener(criteria -> {
            filterCriteria = criteria;
            applyFilter();
        });

        add(filterBar, java.awt.BorderLayout.NORTH);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
    }

    public FilterBar getFilterBar() {
        return filterBar;
    }

    public JTable getTable() {
        return table;
    }

    public void setActiveProfile(ScoringProfile profile) {
        tableModel.setActiveProfile(profile);
        configureColumnWidths();
        applyFilter();
    }

    public void setGarageDimensions(GarageDimensions garageDimensions) {
        this.garageDimensions = garageDimensions;
        applyFilter();
    }

    public void loadVehicles(List<Vehicle> vehicles) {
        filterBar.updateFleetPrices(vehicles);
        allVehicles = new ArrayList<>(vehicles);
        applyFilter();
    }

    public Vehicle getSelectedVehicle() {
        int leadViewRow = table.getSelectionModel().getLeadSelectionIndex();
        if (leadViewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(leadViewRow);
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return null;
        }
        return tableModel.getVehicleAt(modelRow);
    }

    public List<Vehicle> getSelectedVehicles() {
        int[] viewRows = table.getSelectedRows();
        if (viewRows.length == 0) {
            return List.of();
        }
        Set<UUID> seen = new LinkedHashSet<>();
        List<Vehicle> selected = new ArrayList<>();
        for (int viewRow : viewRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
                continue;
            }
            Vehicle vehicle = tableModel.getVehicleAt(modelRow);
            if (vehicle.getId() != null && seen.add(vehicle.getId())) {
                selected.add(vehicle);
            }
        }
        return List.copyOf(selected);
    }

    public List<Vehicle> getVisibleVehicles() {
        return tableModel.getVehiclesSnapshot();
    }

    public void setSelectedVehicle(UUID vehicleId) {
        table.clearSelection();
        if (vehicleId == null) {
            return;
        }
        for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
            Vehicle vehicle = tableModel.getVehicleAt(modelRow);
            if (vehicleId.equals(vehicle.getId())) {
                int viewRow = table.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }
    }

    private void applyFilter() {
        List<Vehicle> visible = new ArrayList<>();
        for (Vehicle vehicle : allVehicles) {
            if (VehicleFilter.matches(vehicle, filterCriteria, garageDimensions)) {
                visible.add(vehicle);
            }
        }
        tableModel.setVehicles(visible);
    }

    private void configureRenderers() {
        NumberFormat currencyFormat = NumberFormat.getIntegerInstance(new Locale("en", "ZA"));
        table.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value == null) {
                    setText("-");
                } else {
                    setText("R " + currencyFormat.format(value));
                }
            }
        });
        table.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value == null) {
                    setText("-");
                } else {
                    setText(String.format("%.1f", value));
                }
            }
        });
    }

    private void configureColumnWidths() {
        for (int column = 0; column < table.getColumnCount(); column++) {
            if (column >= FIXED_PREFIX_COLUMNS && column < tableModel.scorePer100kColumnIndex()) {
                table.getColumnModel().getColumn(column).setPreferredWidth(65);
            }
        }
        if (tableModel.dataCompletenessColumnIndex() >= 0) {
            table.getColumnModel().getColumn(tableModel.dataCompletenessColumnIndex())
                    .setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        protected void setValue(Object value) {
                            if (value == null) {
                                setText("-");
                            } else {
                                setText(String.format("%.1f%%", value));
                            }
                        }
                    });
            table.getColumnModel().getColumn(tableModel.dataCompletenessColumnIndex()).setPreferredWidth(80);
        }
        if (tableModel.garageClearanceColumnIndex() >= 0) {
            table.getColumnModel().getColumn(tableModel.garageClearanceColumnIndex())
                    .setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        protected void setValue(Object value) {
                            if (value == null) {
                                setText("-");
                            } else {
                                setText(String.valueOf(value));
                            }
                        }
                    });
            table.getColumnModel().getColumn(tableModel.garageClearanceColumnIndex()).setPreferredWidth(80);
        }
    }

    private final class VehicleTableModel extends AbstractTableModel {

        private ScoringProfile activeProfile;
        private List<Metric> topMetrics = List.of();
        private List<Vehicle> vehicles = new ArrayList<>();
        private final List<String> columnNames = new ArrayList<>();

        VehicleTableModel(ScoringProfile activeProfile) {
            setActiveProfile(activeProfile);
        }

        void setActiveProfile(ScoringProfile profile) {
            activeProfile = profile;
            topMetrics = TopWeightedMetrics.topN(profile, TOP_METRIC_COUNT);
            rebuildColumnNames();
            fireTableStructureChanged();
        }

        void setVehicles(List<Vehicle> vehicles) {
            this.vehicles = new ArrayList<>(vehicles);
            fireTableDataChanged();
        }

        Vehicle getVehicleAt(int row) {
            return vehicles.get(row);
        }

        List<Vehicle> getVehiclesSnapshot() {
            return List.copyOf(vehicles);
        }

        int scorePer100kColumnIndex() {
            return FIXED_PREFIX_COLUMNS + topMetrics.size() + SCORE_PER_100K_COLUMN_OFFSET;
        }

        int dataCompletenessColumnIndex() {
            return FIXED_PREFIX_COLUMNS + topMetrics.size() + DATA_COMPLETENESS_COLUMN_OFFSET;
        }

        int garageClearanceColumnIndex() {
            return FIXED_PREFIX_COLUMNS + topMetrics.size() + GARAGE_CLEARANCE_COLUMN_OFFSET;
        }

        @Override
        public int getRowCount() {
            return vehicles.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames.get(column);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) {
                return BigDecimal.class;
            }
            if (columnIndex == 0 || columnIndex == 1 || columnIndex == 2) {
                return String.class;
            }
            if (columnIndex == garageClearanceColumnIndex()) {
                return Integer.class;
            }
            return Double.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Vehicle vehicle = vehicles.get(rowIndex);
            if (columnIndex == 0) {
                return vehicle.getMake();
            }
            if (columnIndex == 1) {
                return vehicle.getModel();
            }
            if (columnIndex == 2) {
                return formatDerivative(vehicle.getDerivative());
            }
            if (columnIndex == 3) {
                return priceValue(vehicle.getPricing());
            }
            if (columnIndex == 4) {
                return overallScoreValue(vehicle.getDerivedMetrics());
            }
            int metricEnd = FIXED_PREFIX_COLUMNS + topMetrics.size();
            if (columnIndex < metricEnd) {
                Metric metric = topMetrics.get(columnIndex - FIXED_PREFIX_COLUMNS);
                return MetricScores.displayScore(vehicle, vehicle.getDerivedMetrics(), metric);
            }
            if (columnIndex == scorePer100kColumnIndex()) {
                return scorePer100kValue(vehicle.getDerivedMetrics());
            }
            if (columnIndex == dataCompletenessColumnIndex()) {
                return ScoringInputCoverage.completenessPercent(vehicle);
            }
            if (columnIndex == garageClearanceColumnIndex()) {
                return GarageClearanceCalculator.clearanceMm(garageDimensions, vehicle.getDimensions());
            }
            return "";
        }

        private void rebuildColumnNames() {
            columnNames.clear();
            columnNames.add("Make");
            columnNames.add("Model");
            columnNames.add("Derivative");
            columnNames.add("Price");
            columnNames.add("Overall Score");
            for (Metric metric : topMetrics) {
                columnNames.add(MetricLabels.displayName(metric, activeProfile));
            }
            columnNames.add("Score/R100k");
            columnNames.add("Data Completeness");
            columnNames.add("Garage Clearance");
        }

        private static String formatDerivative(String derivative) {
            return derivative == null || derivative.isBlank() ? "" : derivative;
        }

        private static BigDecimal priceValue(Pricing pricing) {
            if (pricing == null || pricing.getPriceZar() == null) {
                return null;
            }
            return pricing.getPriceZar();
        }

        private static Double overallScoreValue(DerivedMetrics metrics) {
            if (metrics == null || metrics.getOverallScore() == null) {
                return null;
            }
            return metrics.getOverallScore();
        }

        private static Double scorePer100kValue(DerivedMetrics metrics) {
            if (metrics == null || metrics.getScorePer100k() == null) {
                return null;
            }
            return metrics.getScorePer100k();
        }
    }
}
