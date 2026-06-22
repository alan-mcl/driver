package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import za.driver.model.GarageDimensions;
import za.driver.service.AppServices;

public class GarageDimensionsDialog extends JDialog {

    private final AppServices services;
    private final Consumer<GarageDimensions> onSuccess;
    private final JSpinner garageWidthSpinner = dimensionSpinner(GarageDimensions.DEFAULT_GARAGE_WIDTH_MM);
    private final JSpinner arcRadiusSpinner = dimensionSpinner(GarageDimensions.DEFAULT_ARC_RADIUS_MM);
    private final JSpinner arcStartHeightSpinner = dimensionSpinner(GarageDimensions.DEFAULT_ARC_START_HEIGHT_MM);
    private final JButton okButton = new JButton("OK");
    private final JButton cancelButton = new JButton("Cancel");

    public GarageDimensionsDialog(JFrame owner, AppServices services, Consumer<GarageDimensions> onSuccess) {
        super(owner, "Garage Dimensions", true);
        this.services = services;
        this.onSuccess = onSuccess;

        GarageDimensions current = services.garageConfigService.getGarageDimensions();
        garageWidthSpinner.setValue(current.garageWidthMm());
        arcRadiusSpinner.setValue(current.arcRadiusMm());
        arcStartHeightSpinner.setValue(current.arcStartHeightMm());

        okButton.addActionListener(e -> apply());
        cancelButton.addActionListener(e -> dispose());

        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 8));
        fields.add(new JLabel("Garage width (mm)"));
        fields.add(garageWidthSpinner);
        fields.add(new JLabel("Arc radius (mm)"));
        fields.add(arcRadiusSpinner);
        fields.add(new JLabel("Arc start height (mm)"));
        fields.add(arcStartHeightSpinner);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout(12, 12));
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(fields, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private void apply() {
        GarageDimensions dimensions = new GarageDimensions(
                spinnerValue(garageWidthSpinner),
                spinnerValue(arcRadiusSpinner),
                spinnerValue(arcStartHeightSpinner));

        if (dimensions.garageWidthMm() <= 0
                || dimensions.arcRadiusMm() <= 0
                || dimensions.arcStartHeightMm() <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "All dimensions must be greater than zero.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dimensions.arcRadiusMm() * 2 != dimensions.garageWidthMm()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Arc radius × 2 does not equal garage width (semicircle assumption).\nSave anyway?",
                    "Confirm",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
        }

        BackgroundTasks.run(
                this,
                () -> {
                    services.garageConfigService.save(dimensions);
                    return dimensions;
                },
                saved -> {
                    onSuccess.accept(saved);
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

    private static JSpinner dimensionSpinner(int defaultValue) {
        return new JSpinner(new SpinnerNumberModel(defaultValue, 1, 20_000, 1));
    }

    private static int spinnerValue(JSpinner spinner) {
        return ((Number) spinner.getValue()).intValue();
    }
}
