package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;
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
import javax.swing.border.EmptyBorder;

import za.driver.chart.ScatterPlotAxis;
import za.driver.chart.ScatterPlotBuilder;
import za.driver.chart.ScatterPlotData;
import za.driver.model.ScoringProfile;
import za.driver.model.Vehicle;

public class ScatterPlotDialog extends JDialog {

    private final Supplier<List<Vehicle>> vehicleSupplier;
    private final JComboBox<ScatterPlotAxis> xAxisCombo = new JComboBox<>();
    private final JComboBox<ScatterPlotAxis> yAxisCombo = new JComboBox<>();
    private final JLabel statusLabel = new JLabel(" ");
    private final ScatterPlotPanel plotPanel = new ScatterPlotPanel();
    private ScoringProfile activeProfile;

    public ScatterPlotDialog(
            JFrame owner,
            ScoringProfile activeProfile,
            Supplier<List<Vehicle>> vehicleSupplier,
            Consumer<Vehicle> onPointSelected) {
        super(owner, "Scatter Plot", false);
        this.vehicleSupplier = vehicleSupplier;
        this.activeProfile = activeProfile;

        configureAxisCombo(xAxisCombo);
        configureAxisCombo(yAxisCombo);
        rebuildAxisOptions(ScatterPlotAxis.PRICE, ScatterPlotAxis.OVERALL_SCORE);

        xAxisCombo.addActionListener(e -> {
            if (xAxisCombo.getSelectedItem() == yAxisCombo.getSelectedItem()) {
                yAxisCombo.setSelectedItem(fallbackAxis((ScatterPlotAxis) xAxisCombo.getSelectedItem()));
            }
            refreshData();
        });
        yAxisCombo.addActionListener(e -> {
            if (yAxisCombo.getSelectedItem() == xAxisCombo.getSelectedItem()) {
                xAxisCombo.setSelectedItem(fallbackAxis((ScatterPlotAxis) yAxisCombo.getSelectedItem()));
            }
            refreshData();
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        plotPanel.setPointSelectedListener(onPointSelected);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.add(new JLabel("X axis"));
        controls.add(xAxisCombo);
        controls.add(new JLabel("Y axis"));
        controls.add(yAxisCombo);
        controls.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBorder(new EmptyBorder(8, 8, 0, 8));
        header.add(controls, BorderLayout.NORTH);
        header.add(statusLabel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(plotPanel, BorderLayout.CENTER);

        setSize(900, 600);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        refreshData();
    }

    public void setActiveProfile(ScoringProfile profile) {
        if (profile == null) {
            return;
        }
        ScatterPlotAxis xAxis = (ScatterPlotAxis) xAxisCombo.getSelectedItem();
        ScatterPlotAxis yAxis = (ScatterPlotAxis) yAxisCombo.getSelectedItem();
        activeProfile = profile;
        rebuildAxisOptions(xAxis, yAxis);
        refreshData();
    }

    public void refreshData() {
        ScatterPlotAxis xAxis = (ScatterPlotAxis) xAxisCombo.getSelectedItem();
        ScatterPlotAxis yAxis = (ScatterPlotAxis) yAxisCombo.getSelectedItem();
        List<Vehicle> vehicles = vehicleSupplier.get();
        ScatterPlotData data = ScatterPlotBuilder.build(vehicles, xAxis, yAxis);
        plotPanel.setPlot(data, xAxis, yAxis, activeProfile);
        statusLabel.setText(data.points().size() + " plotted, " + data.skippedCount() + " skipped");
    }

    private void configureAxisCombo(JComboBox<ScatterPlotAxis> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ScatterPlotAxis axis) {
                    setText(axis.label(activeProfile));
                }
                return component;
            }
        });
    }

    private void rebuildAxisOptions(ScatterPlotAxis preferredX, ScatterPlotAxis preferredY) {
        List<ScatterPlotAxis> axes = ScatterPlotAxis.selectableAxes(activeProfile);
        xAxisCombo.setModel(new javax.swing.DefaultComboBoxModel<>(axes.toArray(ScatterPlotAxis[]::new)));
        yAxisCombo.setModel(new javax.swing.DefaultComboBoxModel<>(axes.toArray(ScatterPlotAxis[]::new)));
        xAxisCombo.setSelectedItem(selectAxis(axes, preferredX, ScatterPlotAxis.PRICE));
        yAxisCombo.setSelectedItem(selectAxis(axes, preferredY, ScatterPlotAxis.OVERALL_SCORE));
        if (xAxisCombo.getSelectedItem() == yAxisCombo.getSelectedItem()) {
            yAxisCombo.setSelectedItem(fallbackAxis((ScatterPlotAxis) xAxisCombo.getSelectedItem()));
        }
    }

    private static ScatterPlotAxis selectAxis(
            List<ScatterPlotAxis> axes,
            ScatterPlotAxis preferred,
            ScatterPlotAxis fallback) {
        if (preferred != null && axes.contains(preferred)) {
            return preferred;
        }
        if (axes.contains(fallback)) {
            return fallback;
        }
        return axes.get(0);
    }

    private ScatterPlotAxis fallbackAxis(ScatterPlotAxis selected) {
        List<ScatterPlotAxis> axes = ScatterPlotAxis.selectableAxes(activeProfile);
        for (ScatterPlotAxis axis : axes) {
            if (axis != selected) {
                return axis;
            }
        }
        return ScatterPlotAxis.OVERALL_SCORE;
    }
}
