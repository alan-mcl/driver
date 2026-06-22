package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import za.driver.chart.ScatterPlotAxis;
import za.driver.chart.ScatterPlotBuilder;
import za.driver.chart.ScatterPlotData;
import za.driver.model.Vehicle;

public class ScatterPlotDialog extends JDialog {

    private final Supplier<List<Vehicle>> vehicleSupplier;
    private final JComboBox<ScatterPlotAxis> xAxisCombo = new JComboBox<>(ScatterPlotAxis.values());
    private final JComboBox<ScatterPlotAxis> yAxisCombo = new JComboBox<>(ScatterPlotAxis.values());
    private final JLabel statusLabel = new JLabel(" ");
    private final ScatterPlotPanel plotPanel = new ScatterPlotPanel();

    public ScatterPlotDialog(
            JFrame owner,
            Supplier<List<Vehicle>> vehicleSupplier,
            Consumer<Vehicle> onPointSelected) {
        super(owner, "Scatter Plot", false);
        this.vehicleSupplier = vehicleSupplier;

        xAxisCombo.setSelectedItem(ScatterPlotAxis.PRICE);
        yAxisCombo.setSelectedItem(ScatterPlotAxis.OVERALL_SCORE);

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

    public void refreshData() {
        ScatterPlotAxis xAxis = (ScatterPlotAxis) xAxisCombo.getSelectedItem();
        ScatterPlotAxis yAxis = (ScatterPlotAxis) yAxisCombo.getSelectedItem();
        List<Vehicle> vehicles = vehicleSupplier.get();
        ScatterPlotData data = ScatterPlotBuilder.build(vehicles, xAxis, yAxis);
        plotPanel.setPlot(data, xAxis, yAxis);
        statusLabel.setText(data.points().size() + " plotted, " + data.skippedCount() + " skipped");
    }

    private static ScatterPlotAxis fallbackAxis(ScatterPlotAxis selected) {
        for (ScatterPlotAxis axis : ScatterPlotAxis.values()) {
            if (axis != selected) {
                return axis;
            }
        }
        return ScatterPlotAxis.OVERALL_SCORE;
    }
}
