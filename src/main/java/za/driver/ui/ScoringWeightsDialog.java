package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import za.driver.service.AppServices;

public class ScoringWeightsDialog extends JDialog {

    private final AppServices services;
    private final Consumer<Void> onSuccess;
    private final WeightEditor weightEditor = new WeightEditor();
    private final JButton okButton = new JButton("OK");
    private final JButton cancelButton = new JButton("Cancel");

    public ScoringWeightsDialog(JFrame owner, AppServices services, Consumer<Void> onSuccess) {
        super(owner, "Scoring Weights", true);
        this.services = services;
        this.onSuccess = onSuccess;

        weightEditor.loadFrom(services.activeProfile);
        okButton.setEnabled(weightEditor.weightsSumToHundred());
        okButton.addActionListener(e -> apply());
        cancelButton.addActionListener(e -> dispose());

        weightEditor.addWeightChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                okButton.setEnabled(weightEditor.weightsSumToHundred());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout(12, 12));
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(weightEditor, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
    }

    private void apply() {
        if (!weightEditor.weightsSumToHundred()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Weights must total 100.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BackgroundTasks.run(
                this,
                () -> {
                    services.profileService.updateWeightsAndRecalculateAll(
                            services.activeProfile,
                            weightEditor.buildWeights());
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
}
