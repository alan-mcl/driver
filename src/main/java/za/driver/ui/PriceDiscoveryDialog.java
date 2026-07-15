package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import za.driver.chart.PriceDiscoveryBuilder;
import za.driver.chart.PriceDiscoveryCrossover;
import za.driver.chart.PriceDiscoveryData;
import za.driver.model.ScoringProfile;
import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;

public class PriceDiscoveryDialog extends JDialog {

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getIntegerInstance(new Locale("en", "ZA"));

    private final Supplier<List<Vehicle>> vehicleSupplier;
    private final JComboBox<Vehicle> benchmarkCombo = new JComboBox<>();
    private final JLabel statusLabel = new JLabel(" ");
    private final PriceDiscoveryPanel plotPanel = new PriceDiscoveryPanel();
    private final CrossoverTableModel tableModel = new CrossoverTableModel();
    private ScoringProfile activeProfile;
    private UUID preferredBenchmarkId;
    private boolean updatingBenchmarkCombo;

    public PriceDiscoveryDialog(
            JFrame owner,
            ScoringProfile activeProfile,
            Supplier<List<Vehicle>> vehicleSupplier,
            Consumer<Vehicle> onPointSelected) {
        super(owner, "Price Discovery", false);
        this.vehicleSupplier = vehicleSupplier;
        this.activeProfile = activeProfile;

        benchmarkCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Vehicle vehicle) {
                    setText(VehicleIdentity.label(vehicle));
                }
                return component;
            }
        });
        benchmarkCombo.addActionListener(e -> {
            if (!updatingBenchmarkCombo) {
                refreshData();
            }
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        plotPanel.setPointSelectedListener(onPointSelected);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.add(new JLabel("Benchmark"));
        controls.add(benchmarkCombo);
        controls.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBorder(new EmptyBorder(8, 8, 0, 8));
        header.add(controls, BorderLayout.NORTH);
        header.add(statusLabel, BorderLayout.SOUTH);

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(false);
        configureTableColumns(table);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(plotPanel, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.SOUTH);

        setSize(950, 700);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        refreshData();
    }

    public void setActiveProfile(ScoringProfile profile) {
        if (profile == null) {
            return;
        }
        activeProfile = profile;
        refreshData();
    }

    public void refreshData() {
        List<Vehicle> selected = vehicleSupplier.get();
        UUID benchmarkId = preferredBenchmarkId;
        Vehicle currentBenchmark = (Vehicle) benchmarkCombo.getSelectedItem();
        if (currentBenchmark != null && currentBenchmark.getId() != null) {
            benchmarkId = currentBenchmark.getId();
        } else if (benchmarkId == null) {
            benchmarkId = PriceDiscoveryBuilder.defaultBenchmarkId(selected);
        }

        rebuildBenchmarkCombo(selected, benchmarkId);
        Vehicle benchmark = (Vehicle) benchmarkCombo.getSelectedItem();
        UUID resolvedBenchmarkId = benchmark != null ? benchmark.getId() : benchmarkId;

        PriceDiscoveryData data = PriceDiscoveryBuilder.build(selected, resolvedBenchmarkId);
        plotPanel.setPlot(data);
        tableModel.setCrossovers(data.crossovers());

        int plottable = (data.benchmark() != null ? 1 : 0) + data.subjects().size();
        statusLabel.setText(plottable + " plotted, " + data.skippedCount() + " skipped");
        preferredBenchmarkId = resolvedBenchmarkId;
    }

    private void rebuildBenchmarkCombo(List<Vehicle> selected, UUID benchmarkId) {
        List<Vehicle> candidates = plottableVehicles(selected);
        updatingBenchmarkCombo = true;
        try {
            benchmarkCombo.removeAllItems();
            Vehicle chosen = null;
            for (Vehicle vehicle : candidates) {
                benchmarkCombo.addItem(vehicle);
                if (benchmarkId != null && benchmarkId.equals(vehicle.getId())) {
                    chosen = vehicle;
                }
            }
            if (chosen != null) {
                benchmarkCombo.setSelectedItem(chosen);
            } else if (!candidates.isEmpty()) {
                UUID defaultId = PriceDiscoveryBuilder.defaultBenchmarkId(selected);
                for (int i = 0; i < benchmarkCombo.getItemCount(); i++) {
                    Vehicle vehicle = benchmarkCombo.getItemAt(i);
                    if (defaultId != null && defaultId.equals(vehicle.getId())) {
                        benchmarkCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } finally {
            updatingBenchmarkCombo = false;
        }
    }

    private static List<Vehicle> plottableVehicles(List<Vehicle> selected) {
        List<Vehicle> candidates = new ArrayList<>();
        if (selected == null) {
            return candidates;
        }
        for (Vehicle vehicle : selected) {
            if (vehicle == null) {
                continue;
            }
            if (vehicle.getPricing() == null || vehicle.getPricing().getPriceZar() == null) {
                continue;
            }
            if (vehicle.getDerivedMetrics() == null || vehicle.getDerivedMetrics().getOverallScore() == null) {
                continue;
            }
            if (vehicle.getPricing().getPriceZar().doubleValue() <= 0.0) {
                continue;
            }
            candidates.add(vehicle);
        }
        return candidates;
    }

    private static void configureTableColumns(JTable table) {
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number number) {
                    setText("R" + CURRENCY_FORMAT.format(Math.round(number.doubleValue())));
                } else {
                    setText(value == null ? "" : value.toString());
                }
            }
        };
        DefaultTableCellRenderer scoreRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number number) {
                    setText(String.format(Locale.ROOT, "%.1f", number.doubleValue()));
                } else {
                    setText(value == null ? "" : value.toString());
                }
            }
        };
        DefaultTableCellRenderer discountRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof CrossoverTableModel.DiscountCell cell) {
                    if (cell.beatsAtList()) {
                        setText("—");
                    } else {
                        setText(String.format(
                                Locale.ROOT,
                                "−R%s / −%.1f%%",
                                CURRENCY_FORMAT.format(Math.round(cell.discountZar())),
                                cell.discountPct()));
                    }
                } else {
                    setText("");
                }
            }
        };

        table.getColumnModel().getColumn(1).setCellRenderer(currencyRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(scoreRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(discountRenderer);
    }

    private static final class CrossoverTableModel extends AbstractTableModel {

        private record DiscountCell(double discountZar, double discountPct, boolean beatsAtList) {
        }

        private static final String[] COLUMNS = {
                "Vehicle",
                "List price",
                "List Score/R100k",
                "Target price vs benchmark",
                "Discount",
                "Status"
        };

        private List<PriceDiscoveryCrossover> crossovers = List.of();

        void setCrossovers(List<PriceDiscoveryCrossover> crossovers) {
            this.crossovers = crossovers != null ? List.copyOf(crossovers) : List.of();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return crossovers.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PriceDiscoveryCrossover crossover = crossovers.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> crossover.subjectLabel();
                case 1 -> crossover.listPrice();
                case 2 -> crossover.listScorePer100k();
                case 3 -> crossover.beatsAtList() ? null : crossover.crossoverPrice();
                case 4 -> new DiscountCell(crossover.discountZar(), crossover.discountPct(), crossover.beatsAtList());
                case 5 -> crossover.beatsAtList() ? "Beats at list" : "Needs negotiation";
                default -> "";
            };
        }
    }
}
