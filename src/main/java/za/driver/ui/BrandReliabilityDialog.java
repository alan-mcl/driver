package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import za.driver.model.BrandReliabilityConfig;
import za.driver.model.BrandReliabilityEntry;
import za.driver.service.AppServices;

public class BrandReliabilityDialog extends JDialog {

    private final AppServices services;
    private final Consumer<Void> onSuccess;
    private final BrandTableModel tableModel = new BrandTableModel();
    private final JTable table = new JTable(tableModel);
    private final JButton okButton = new JButton("OK");
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton addBrandButton = new JButton("Add Brand…");

    public BrandReliabilityDialog(JFrame owner, AppServices services, Consumer<Void> onSuccess) {
        super(owner, "Brand Reliability", true);
        this.services = services;
        this.onSuccess = onSuccess;

        tableModel.loadFrom(services.brandReliabilityConfigService.getEditableConfig());

        okButton.addActionListener(e -> apply());
        cancelButton.addActionListener(e -> dispose());
        addBrandButton.addActionListener(e -> addBrand());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(addBrandButton);
        buttons.add(okButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout(12, 12));
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(new JLabel("Edit base reliability and confidence scores by manufacturer (0–100)."),
                BorderLayout.NORTH);
        content.add(buttons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(owner);
    }

    private void addBrand() {
        JTextField nameField = new JTextField();
        JSpinner reliabilitySpinner = scoreSpinner(75);
        JSpinner confidenceSpinner = scoreSpinner(50);
        JPanel panel = new JPanel(new java.awt.GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Brand name"));
        panel.add(nameField);
        panel.add(new JLabel("Reliability"));
        panel.add(reliabilitySpinner);
        panel.add(new JLabel("Confidence"));
        panel.add(confidenceSpinner);

        int choice = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Brand",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brand name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tableModel.hasBrand(name)) {
            JOptionPane.showMessageDialog(this, "Brand already exists: " + name, "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.addBrand(name, spinnerValue(reliabilitySpinner), spinnerValue(confidenceSpinner));
    }

    private void apply() {
        try {
            BrandReliabilityConfig config = tableModel.toConfig();
            services.brandReliabilityConfigService.validate(config);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BackgroundTasks.run(
                this,
                () -> {
                    services.brandReliabilityConfigService.saveAndRecalculateAll(
                            tableModel.toConfig(),
                            services.activeProfile,
                            services.profileService);
                    return null;
                },
                ignored -> {
                    onSuccess.accept(null);
                    dispose();
                },
                error -> {
                    if (error instanceof IllegalArgumentException) {
                        JOptionPane.showMessageDialog(
                                this,
                                error.getMessage(),
                                "Validation",
                                JOptionPane.WARNING_MESSAGE);
                    } else if (error instanceof IOException) {
                        BackgroundTasks.showError(this, "Save Failed", error);
                    } else {
                        BackgroundTasks.showError(this, "Update Failed", error);
                    }
                });
    }

    private static JSpinner scoreSpinner(int defaultValue) {
        return new JSpinner(new SpinnerNumberModel(defaultValue, 0, 100, 1));
    }

    private static int spinnerValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }

    private static final class BrandTableModel extends AbstractTableModel {

        private final List<String> brandNames = new ArrayList<>();
        private final Map<String, BrandReliabilityEntry> entries = new LinkedHashMap<>();

        void loadFrom(BrandReliabilityConfig config) {
            brandNames.clear();
            entries.clear();
            if (config == null || config.getBrands() == null) {
                fireTableDataChanged();
                return;
            }
            config.getBrands().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                    .forEach(entry -> {
                        brandNames.add(entry.getKey());
                        entries.put(entry.getKey(), copyEntry(entry.getValue()));
                    });
            fireTableDataChanged();
        }

        BrandReliabilityConfig toConfig() {
            BrandReliabilityConfig config = new BrandReliabilityConfig();
            config.setSchemaVersion(1);
            Map<String, BrandReliabilityEntry> brands = new LinkedHashMap<>();
            for (String brandName : brandNames) {
                BrandReliabilityEntry entry = entries.get(brandName);
                if (entry != null) {
                    brands.put(brandName, copyEntry(entry));
                }
            }
            config.setBrands(brands);
            return config;
        }

        boolean hasBrand(String name) {
            return brandNames.stream().anyMatch(existing -> existing.equalsIgnoreCase(name.trim()));
        }

        void addBrand(String name, int reliability, int confidence) {
            brandNames.add(name.trim());
            entries.put(name.trim(), new BrandReliabilityEntry(reliability, confidence));
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return brandNames.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Brand";
                case 1 -> "Reliability";
                case 2 -> "Confidence";
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String brandName = brandNames.get(rowIndex);
            BrandReliabilityEntry entry = entries.get(brandName);
            if (entry == null) {
                return null;
            }
            return switch (columnIndex) {
                case 0 -> brandName;
                case 1 -> entry.getReliability();
                case 2 -> entry.getConfidence();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            String brandName = brandNames.get(rowIndex);
            BrandReliabilityEntry entry = entries.computeIfAbsent(brandName, ignored -> new BrandReliabilityEntry());
            if (columnIndex == 1) {
                entry.setReliability(((Number) value).intValue());
            } else if (columnIndex == 2) {
                entry.setConfidence(((Number) value).intValue());
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        private static BrandReliabilityEntry copyEntry(BrandReliabilityEntry source) {
            if (source == null) {
                return new BrandReliabilityEntry();
            }
            return new BrandReliabilityEntry(source.getReliability(), source.getConfidence());
        }
    }
}
