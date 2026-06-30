package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import za.driver.model.BodyType;
import za.driver.model.DerivedMetrics;
import za.driver.model.Dimensions;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.Infotainment;
import za.driver.model.Metric;
import za.driver.model.Ownership;
import za.driver.model.Performance;
import za.driver.model.Pricing;
import za.driver.model.Safety;
import za.driver.model.ScoringProfile;
import za.driver.model.Source;
import za.driver.model.Towing;
import za.driver.model.Transmission;
import za.driver.model.Vehicle;
import za.driver.model.Wheels;
import za.driver.presentation.BodyTypeLabels;
import za.driver.presentation.MetricLabels;
import za.driver.presentation.VehicleSortOrder;
import za.driver.model.VehicleIdentity;
import za.driver.scoring.MetricScores;

public class ComparisonDialog extends JDialog {

    private static final int VEHICLE_COLUMN_COUNT = 4;
    private static final int LABEL_COLUMN_WIDTH = 220;
    private static final int VALUE_COLUMN_WIDTH = 180;
    private static final int HEADER_HEIGHT = 34;

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getIntegerInstance(new Locale("en", "ZA"));

    private final ComparisonTableModel tableModel;
    private final Vehicle[] selected = new Vehicle[VEHICLE_COLUMN_COUNT];
    private final JComboBox<Vehicle>[] vehicleCombos = new JComboBox[VEHICLE_COLUMN_COUNT];

    public ComparisonDialog(
            JFrame owner,
            List<Vehicle> allVehicles,
            List<Vehicle> initialSelection,
            ScoringProfile activeProfile) {
        super(owner, "Compare Vehicles", false);
        this.tableModel = new ComparisonTableModel(buildRows(activeProfile), selected);

        List<Vehicle> sortedVehicles = new ArrayList<>(allVehicles);
        sortedVehicles.sort(VehicleSortOrder.byMakeModelPrice());

        JTable table = new JTable(tableModel);
        table.setTableHeader(null);
        table.setShowGrid(false);
        table.setRowHeight(22);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setDefaultRenderer(Object.class, new ComparisonCellRenderer());
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(LABEL_COLUMN_WIDTH);
        for (int column = 1; column <= VEHICLE_COLUMN_COUNT; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(VALUE_COLUMN_WIDTH);
        }

        JPanel comboHeaderPanel = createComboHeaderPanel(table);

        DefaultListCellRenderer comboRenderer = createComboRenderer();
        for (int column = 0; column < VEHICLE_COLUMN_COUNT; column++) {
            JComboBox<Vehicle> combo = new JComboBox<>();
            combo.setRenderer(comboRenderer);
            combo.setMaximumRowCount(16);
            combo.addItem(null);
            for (Vehicle vehicle : sortedVehicles) {
                combo.addItem(vehicle);
            }
            int finalColumn = column;
            combo.addActionListener(e -> {
                selected[finalColumn] = (Vehicle) combo.getSelectedItem();
                tableModel.fireTableDataChanged();
            });
            vehicleCombos[column] = combo;
            comboHeaderPanel.add(combo);
        }

        if (initialSelection != null) {
            for (int column = 0; column < VEHICLE_COLUMN_COUNT && column < initialSelection.size(); column++) {
                Vehicle vehicle = initialSelection.get(column);
                vehicleCombos[column].setSelectedItem(vehicle);
                selected[column] = vehicle;
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(comboHeaderPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().addChangeListener(e -> {
            comboHeaderPanel.revalidate();
            comboHeaderPanel.repaint();
        });
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnMarginChanged(ChangeEvent e) {
                comboHeaderPanel.revalidate();
                comboHeaderPanel.repaint();
            }

            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(comboHeaderPanel, BorderLayout.NORTH);

        setSize(1280, 860);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        SwingUtilities.invokeLater(() -> {
            comboHeaderPanel.revalidate();
            comboHeaderPanel.repaint();
        });
    }

    private static JPanel createComboHeaderPanel(JTable table) {
        JPanel panel = new JPanel((java.awt.LayoutManager) null) {
            @Override
            public void doLayout() {
                int x = table.getColumnModel().getColumn(0).getWidth();
                int height = getHeight() > 0 ? getHeight() : HEADER_HEIGHT;
                int comboHeight = Math.min(26, height - 6);
                int y = Math.max(2, (height - comboHeight) / 2);
                for (int i = 0; i < getComponentCount() && i + 1 < table.getColumnCount(); i++) {
                    int width = table.getColumnModel().getColumn(i + 1).getWidth();
                    getComponent(i).setBounds(x + 4, y, Math.max(0, width - 8), comboHeight);
                    x += width;
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(table.getColumnModel().getTotalColumnWidth(), HEADER_HEIGHT);
            }
        };
        panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(225, 225, 225)));
        return panel;
    }

    private static DefaultListCellRenderer createComboRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("(none)");
                } else if (value instanceof Vehicle vehicle) {
                    setText(VehicleIdentity.label(vehicle));
                }
                return component;
            }
        };
    }

    private static List<ComparisonRow> buildRows(ScoringProfile profile) {
        List<ComparisonRow> rows = new ArrayList<>();

        rows.add(field("List price", ComparisonDialog::formatPrice));

        addSection(rows, "General");
        rows.add(field("Make", Vehicle::getMake));
        rows.add(field("Model", Vehicle::getModel));
        rows.add(field("Derivative", Vehicle::getDerivative));
        rows.add(field("Model year", v -> formatInteger(v.getModelYear())));
        rows.add(field("Body type", v -> formatBodyType(v.getBodyType())));
        rows.add(field("Status", v -> formatEnum(v.getStatus())));

        addSection(rows, "Engine");
        rows.add(field("Fuel type", v -> formatEnum(engine(v).getFuelType())));
        rows.add(field("Displacement (cc)", v -> formatInteger(engine(v).getDisplacementCc())));
        rows.add(field("Cylinders", v -> formatInteger(engine(v).getCylinders())));
        rows.add(field("Power (kW)", v -> formatDouble(engine(v).getPowerKw())));
        rows.add(field("Torque (Nm)", v -> formatDouble(engine(v).getTorqueNm())));
        rows.add(field("Aspiration", v -> formatEnum(engine(v).getAspiration())));
        rows.add(field("Hybrid", v -> formatBoolean(engine(v).getHybrid())));
        rows.add(field("PHEV", v -> formatBoolean(engine(v).getPhev())));

        addSection(rows, "Transmission & Performance");
        rows.add(field("Type", v -> formatEnum(transmission(v).getType())));
        rows.add(field("Gears", v -> formatInteger(transmission(v).getGears())));
        rows.add(field("Drivetrain", v -> formatEnum(transmission(v).getDrivetrain())));
        rows.add(field("0–100 km/h (s)", v -> formatDouble(performance(v).getZeroToHundredSeconds())));
        rows.add(field("Top speed (km/h)", v -> formatInteger(performance(v).getTopSpeedKmh())));

        addSection(rows, "Dimensions");
        rows.add(field("Length (mm)", v -> formatInteger(dimensions(v).getLengthMm())));
        rows.add(field("Width (mm)", v -> formatInteger(dimensions(v).getWidthMm())));
        rows.add(field("Height (mm)", v -> formatInteger(dimensions(v).getHeightMm())));
        rows.add(field("Wheelbase (mm)", v -> formatInteger(dimensions(v).getWheelbaseMm())));
        rows.add(field("Ground clearance (mm)", v -> formatInteger(dimensions(v).getGroundClearanceMm())));
        rows.add(field("Turning circle (m)", v -> formatDouble(dimensions(v).getTurningCircleM())));
        rows.add(field("Boot (L)", v -> formatInteger(dimensions(v).getBootLitres())));
        rows.add(field("Kerb weight (kg)", v -> formatInteger(dimensions(v).getKerbWeightKg())));
        rows.add(field("Seats", v -> formatInteger(dimensions(v).getSeats())));
        rows.add(field("Towing braked (kg)", v -> formatInteger(towing(v).getTowingBrakedKg())));

        addSection(rows, "Wheels");
        rows.add(field("Tyre size", v -> formatString(wheels(v).getTyreSize())));

        addSection(rows, "Infotainment");
        rows.add(field("Screen size (in)", v -> formatDouble(infotainment(v).getInfotainmentScreenSizeInches())));
        rows.add(field("Speakers", v -> formatInteger(infotainment(v).getSpeakerCount())));

        addSection(rows, "Economy");
        rows.add(field("Fuel consumption combined", v -> formatDouble(economy(v).getFuelConsumptionCombined())));
        rows.add(field("Fuel tank (L)", v -> formatDouble(economy(v).getFuelTankLitres())));
        rows.add(field("CO2 (g/km)", v -> formatDouble(economy(v).getCo2Gkm())));

        addSection(rows, "Safety");
        rows.add(field("NCAP stars", v -> formatInteger(safety(v).getNcapStars())));
        rows.add(field("Airbags", v -> formatInteger(safety(v).getAirbags())));
        rows.add(field("ABS", v -> formatBoolean(safety(v).getAbs())));
        rows.add(field("ESP", v -> formatBoolean(safety(v).getEsp())));
        rows.add(field("Traction control", v -> formatBoolean(safety(v).getTractionControl())));
        rows.add(field("AEB", v -> formatBoolean(safety(v).getAeb())));
        rows.add(field("Lane assist", v -> formatBoolean(safety(v).getLaneAssist())));
        rows.add(field("Blind spot monitoring", v -> formatBoolean(safety(v).getBlindSpotMonitoring())));
        rows.add(field("Adaptive cruise", v -> formatBoolean(safety(v).getAdaptiveCruiseControl())));
        rows.add(field("Rear cross-traffic alert", v -> formatBoolean(safety(v).getRearCrossTrafficAlert())));

        addSection(rows, "Features");
        rows.add(field("Android Auto", v -> formatBoolean(features(v).getAndroidAuto())));
        rows.add(field("Apple CarPlay", v -> formatBoolean(features(v).getAppleCarplay())));
        rows.add(field("Reverse camera", v -> formatBoolean(features(v).getReverseCamera())));
        rows.add(field("Parking sensors front", v -> formatBoolean(features(v).getParkingSensorsFront())));
        rows.add(field("Parking sensors rear", v -> formatBoolean(features(v).getParkingSensorsRear())));
        rows.add(field("Digital cluster", v -> formatBoolean(features(v).getDigitalCluster())));
        rows.add(field("Keyless entry", v -> formatBoolean(features(v).getKeylessEntry())));
        rows.add(field("Push-button start", v -> formatBoolean(features(v).getPushButtonStart())));
        rows.add(field("Wireless charging", v -> formatBoolean(features(v).getWirelessCharging())));
        rows.add(field("Climate control", v -> formatBoolean(features(v).getClimateControl())));
        rows.add(field("Climate control type", v -> formatEnum(features(v).getClimateControlType())));
        rows.add(field("Heated seats", v -> formatBoolean(features(v).getHeatedSeats())));
        rows.add(field("Electric seats", v -> formatBoolean(features(v).getElectricSeats())));
        rows.add(field("Sunroof", v -> formatBoolean(features(v).getSunroof())));
        rows.add(field("Premium audio", v -> formatBoolean(features(v).getPremiumAudio())));

        addSection(rows, "Ownership");
        rows.add(field("Warranty years", v -> formatInteger(ownership(v).getWarrantyYears())));
        rows.add(field("Warranty km", v -> formatInteger(ownership(v).getWarrantyKm())));
        rows.add(field("Service plan years", v -> formatInteger(ownership(v).getServicePlanYears())));
        rows.add(field("Service plan km", v -> formatInteger(ownership(v).getServicePlanKm())));
        rows.add(field("Service interval km", v -> formatInteger(ownership(v).getServiceIntervalKm())));
        rows.add(field("Maintenance plan years", v -> formatInteger(ownership(v).getMaintenancePlanYears())));
        rows.add(field("Maintenance plan km", v -> formatInteger(ownership(v).getMaintenancePlanKm())));
        rows.add(field("Parts support score (0-100)", v -> formatInteger(ownership(v).getPartsSupportScore())));
        rows.add(field("Locally produced", v -> formatBoolean(ownership(v).getLocalProduction())));

        addSection(rows, "Pricing");
        rows.add(field("Price date (YYYY-MM-DD)", v -> formatDate(pricing(v).getPriceDate())));

        addSection(rows, "Source");
        rows.add(field("Source type", v -> formatEnum(source(v).getSourceType())));
        rows.add(field("Source name", v -> formatString(source(v).getSourceName())));
        rows.add(field("Source URL", v -> formatString(source(v).getSourceUrl())));
        rows.add(field("Imported date", v -> formatDateTime(source(v).getImportedDate())));

        addSection(rows, "Scores");
        rows.add(field("Safety", v -> formatScore(MetricScores.displayScore(v, metrics(v), Metric.SAFETY))));
        rows.add(field("Running cost", v -> formatScore(metrics(v).getRunningCostScore())));
        rows.add(field("Reliability", v -> formatScore(MetricScores.displayScore(v, metrics(v), Metric.RELIABILITY))));
        rows.add(field("Comfort", v -> formatScore(metrics(v).getComfortScore())));
        rows.add(field("Performance", v -> formatScore(metrics(v).getPerformanceScore())));
        rows.add(field("Daily driver", v -> formatScore(metrics(v).getDailyDriverScore())));
        rows.add(field("Technology", v -> formatScore(metrics(v).getTechnologyScore())));
        rows.add(field("Prestige", v -> formatScore(metrics(v).getPrestigeScore())));
        rows.add(field(MetricLabels.displayName(Metric.AWESOMENESS, profile),
                v -> formatScore(metrics(v).getAwesomenessScore())));
        rows.add(field("Overall", v -> formatScore(metrics(v).getOverallScore())));
        rows.add(field("Score/R100k", v -> formatScore(metrics(v).getScorePer100k())));

        return rows;
    }

    private static void addSection(List<ComparisonRow> rows, String title) {
        rows.add(ComparisonRow.spacer());
        rows.add(ComparisonRow.section(title));
    }

    private static ComparisonRow field(String label, Function<Vehicle, String> extractor) {
        return ComparisonRow.field(label, extractor);
    }

    private static Engine engine(Vehicle vehicle) {
        Engine engine = vehicle.getEngine();
        return engine != null ? engine : new Engine();
    }

    private static Transmission transmission(Vehicle vehicle) {
        Transmission transmission = vehicle.getTransmission();
        return transmission != null ? transmission : new Transmission();
    }

    private static Performance performance(Vehicle vehicle) {
        Performance performance = vehicle.getPerformance();
        return performance != null ? performance : new Performance();
    }

    private static Dimensions dimensions(Vehicle vehicle) {
        Dimensions dimensions = vehicle.getDimensions();
        return dimensions != null ? dimensions : new Dimensions();
    }

    private static Towing towing(Vehicle vehicle) {
        Towing towing = vehicle.getTowing();
        return towing != null ? towing : new Towing();
    }

    private static Wheels wheels(Vehicle vehicle) {
        Wheels wheels = vehicle.getWheels();
        return wheels != null ? wheels : new Wheels();
    }

    private static Infotainment infotainment(Vehicle vehicle) {
        Infotainment infotainment = vehicle.getInfotainment();
        return infotainment != null ? infotainment : new Infotainment();
    }

    private static Economy economy(Vehicle vehicle) {
        Economy economy = vehicle.getEconomy();
        return economy != null ? economy : new Economy();
    }

    private static Safety safety(Vehicle vehicle) {
        Safety safety = vehicle.getSafety();
        return safety != null ? safety : new Safety();
    }

    private static Features features(Vehicle vehicle) {
        Features features = vehicle.getFeatures();
        return features != null ? features : new Features();
    }

    private static Ownership ownership(Vehicle vehicle) {
        Ownership ownership = vehicle.getOwnership();
        return ownership != null ? ownership : new Ownership();
    }

    private static Pricing pricing(Vehicle vehicle) {
        Pricing pricing = vehicle.getPricing();
        return pricing != null ? pricing : new Pricing();
    }

    private static Source source(Vehicle vehicle) {
        Source source = vehicle.getSource();
        return source != null ? source : new Source();
    }

    private static DerivedMetrics metrics(Vehicle vehicle) {
        DerivedMetrics metrics = vehicle.getDerivedMetrics();
        return metrics != null ? metrics : new DerivedMetrics();
    }

    private static String formatPrice(Vehicle vehicle) {
        Pricing pricing = vehicle.getPricing();
        if (pricing == null || pricing.getPriceZar() == null) {
            return "-";
        }
        return "R " + CURRENCY_FORMAT.format(pricing.getPriceZar());
    }

    private static String formatString(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
    }

    private static String formatInteger(Integer value) {
        return value == null ? "-" : value.toString();
    }

    private static String formatDouble(Double value) {
        return value == null ? "-" : String.format("%.1f", value);
    }

    private static String formatScore(Double value) {
        return value == null ? "-" : String.format("%.1f", value);
    }

    private static String formatBoolean(Boolean value) {
        if (value == null) {
            return "-";
        }
        return Boolean.TRUE.equals(value) ? "\u2713" : "";
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "-" : value.toString();
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.toString();
    }

    private static String formatBodyType(BodyType bodyType) {
        if (bodyType == null) {
            return "-";
        }
        return BodyTypeLabels.displayName(bodyType);
    }

    private static String formatEnum(Enum<?> value) {
        if (value == null) {
            return "-";
        }
        return titleCaseEnum(value.name());
    }

    private static String titleCaseEnum(String name) {
        String normalized = name.replace('_', ' ').toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return normalized;
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private enum RowKind {
        FIELD,
        SECTION,
        SPACER
    }

    private record ComparisonRow(RowKind kind, String label, Function<Vehicle, String> extractor) {

        static ComparisonRow field(String label, Function<Vehicle, String> extractor) {
            return new ComparisonRow(RowKind.FIELD, label, extractor);
        }

        static ComparisonRow section(String label) {
            return new ComparisonRow(RowKind.SECTION, label, null);
        }

        static ComparisonRow spacer() {
            return new ComparisonRow(RowKind.SPACER, "", null);
        }
    }

    private static final class ComparisonTableModel extends AbstractTableModel {

        private final List<ComparisonRow> rows;
        private final Vehicle[] selected;

        ComparisonTableModel(List<ComparisonRow> rows, Vehicle[] selected) {
            this.rows = rows;
            this.selected = selected;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return VEHICLE_COLUMN_COUNT + 1;
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Field" : "Vehicle " + column;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ComparisonRow row = rows.get(rowIndex);
            if (columnIndex == 0) {
                return row.label();
            }
            Vehicle vehicle = selected[columnIndex - 1];
            if (vehicle == null || row.kind() != RowKind.FIELD || row.extractor() == null) {
                return "";
            }
            return row.extractor().apply(vehicle);
        }

        ComparisonRow rowAt(int rowIndex) {
            return rows.get(rowIndex);
        }
    }

    private final class ComparisonCellRenderer extends DefaultTableCellRenderer {

        private final Color sectionBackground = new Color(240, 240, 240);
        private final Color gridColor = new Color(225, 225, 225);

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            ComparisonRow comparisonRow = tableModel.rowAt(row);

            int rightBorder = column < table.getColumnCount() - 1 ? 1 : 0;
            setBorder(new MatteBorder(0, 0, 1, rightBorder, gridColor));

            if (comparisonRow.kind() == RowKind.SPACER) {
                setText("");
                setBackground(Color.WHITE);
                setForeground(table.getForeground());
                setFont(getFont().deriveFont(Font.PLAIN));
                setHorizontalAlignment(LEFT);
                return component;
            }

            if (comparisonRow.kind() == RowKind.SECTION) {
                setText(column == 0 ? comparisonRow.label() : "");
                setBackground(sectionBackground);
                setForeground(table.getForeground());
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(LEFT);
                return component;
            }

            setBackground(Color.WHITE);
            setForeground(table.getForeground());
            setFont(getFont().deriveFont(Font.PLAIN));
            if (column == 0) {
                setText(comparisonRow.label());
                setHorizontalAlignment(LEFT);
            } else {
                setText(value == null ? "" : value.toString());
                setHorizontalAlignment("\u2713".equals(value) ? CENTER : LEFT);
            }
            return component;
        }
    }
}
