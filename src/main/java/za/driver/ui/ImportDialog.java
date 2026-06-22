package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import za.driver.import_.ImportResult;
import za.driver.import_.ImportVehicleEntry;
import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;
import za.driver.service.AppServices;

public class ImportDialog extends JDialog {

    private static final String PLACEHOLDER = "Paste JSON here, or use Browse… to load a file.";

    private final AppServices services;
    private final Consumer<UUID> onSuccess;

    private final JLabel fileLabel = new JLabel("No file loaded");
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea jsonArea = new JTextArea(16, 60);
    private final JButton previewButton = new JButton("Preview");
    private final JButton importButton = new JButton("Import");
    private final JButton browseButton = new JButton("Browse…");
    private final JButton cancelButton = new JButton("Cancel");

    private ImportResult currentResult;
    private boolean showingPlaceholder;

    public ImportDialog(JFrame owner, AppServices services, Consumer<UUID> onSuccess) {
        super(owner, "Import JSON", true);
        this.services = services;
        this.onSuccess = onSuccess;

        jsonArea.setEditable(true);
        jsonArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, jsonArea.getFont().getSize()));
        jsonArea.setLineWrap(true);
        jsonArea.setWrapStyleWord(true);
        showPlaceholder();
        previewButton.setEnabled(false);
        importButton.setEnabled(false);

        browseButton.addActionListener(e -> browse());
        previewButton.addActionListener(e -> previewEditorContent());
        importButton.addActionListener(e -> importVehicle());
        cancelButton.addActionListener(e -> dispose());

        jsonArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onEditorChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onEditorChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onEditorChanged();
            }
        });

        jsonArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (showingPlaceholder) {
                    jsonArea.setText("");
                    showingPlaceholder = false;
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (jsonArea.getText().isBlank()) {
                    showPlaceholder();
                }
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(browseButton);
        topPanel.add(fileLabel);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.add(new JScrollPane(jsonArea), BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(previewButton);
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout(8, 8));
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void showPlaceholder() {
        showingPlaceholder = true;
        jsonArea.setText(PLACEHOLDER);
        jsonArea.setForeground(java.awt.Color.GRAY);
    }

    private void onEditorChanged() {
        if (!showingPlaceholder) {
            jsonArea.setForeground(java.awt.Color.BLACK);
        }
        previewButton.setEnabled(hasEditorContent());
        importButton.setEnabled(false);
        currentResult = null;
        if (!hasEditorContent()) {
            statusLabel.setText(" ");
        }
    }

    private boolean hasEditorContent() {
        return !showingPlaceholder && !jsonArea.getText().isBlank();
    }

    private String editorJson() {
        if (showingPlaceholder || jsonArea.getText().isBlank()) {
            return null;
        }
        return jsonArea.getText();
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = chooser.getSelectedFile();
        fileLabel.setText(selectedFile.getName());
        BackgroundTasks.run(
                this,
                () -> Files.readString(selectedFile.toPath()),
                json -> {
                    showingPlaceholder = false;
                    jsonArea.setForeground(java.awt.Color.BLACK);
                    jsonArea.setText(json);
                    previewJson(json);
                },
                error -> {
                    currentResult = null;
                    importButton.setEnabled(false);
                    statusLabel.setText("Error reading file: " + error.getMessage());
                    BackgroundTasks.showError(this, "Import Preview", error);
                });
    }

    private void previewEditorContent() {
        String json = editorJson();
        if (json == null) {
            return;
        }
        previewJson(json);
    }

    private void previewJson(String json) {
        currentResult = services.importService.parse(json);
        if (!currentResult.isValid()) {
            showInvalidStatus(currentResult);
            return;
        }
        try {
            updateStatus(currentResult);
        } catch (IOException e) {
            currentResult = null;
            importButton.setEnabled(false);
            statusLabel.setText("Error checking existing records: " + e.getMessage());
            BackgroundTasks.showError(this, "Import Preview", e);
        }
    }

    private void showInvalidStatus(ImportResult result) {
        importButton.setEnabled(false);
        statusLabel.setText(String.join(" ", result.getErrors()));
    }

    private void updateStatus(ImportResult result) throws IOException {
        if (!result.isValid()) {
            showInvalidStatus(result);
            return;
        }

        int total = result.getVehicleCount();
        if (total == 1) {
            ImportVehicleEntry entry = result.getEntries().get(0);
            Vehicle vehicle = entry.getVehicle();
            Optional<Vehicle> existing = services.vehicleService.findByIdentity(
                    vehicle.getMake(),
                    vehicle.getModel(),
                    vehicle.getDerivative());
            String action = existing.isPresent() ? "Ready to update" : "Ready to import";
            StringBuilder summary = new StringBuilder(action).append(": ");
            summary.append(VehicleIdentity.label(vehicle));
            summary.append(" (").append(vehicle.getStatus()).append(")");
            if (!entry.getDataQuality().isEmpty()) {
                summary.append(" — ").append(entry.getDataQuality().size()).append(" data quality flags");
            }
            statusLabel.setText(summary.toString());
        } else {
            int updates = countUpdates(result.getEntries());
            int creates = total - updates;
            StringBuilder summary = new StringBuilder("Ready to import ").append(total).append(" vehicles: ");
            summary.append(creates).append(" new");
            if (updates > 0) {
                summary.append(", ").append(updates).append(" update").append(updates == 1 ? "" : "s");
            }
            statusLabel.setText(summary.toString());
        }
        importButton.setEnabled(true);
    }

    private int countUpdates(List<ImportVehicleEntry> entries) throws IOException {
        int updates = 0;
        for (ImportVehicleEntry entry : entries) {
            Vehicle vehicle = entry.getVehicle();
            if (services.vehicleService.findByIdentity(
                    vehicle.getMake(),
                    vehicle.getModel(),
                    vehicle.getDerivative()).isPresent()) {
                updates++;
            }
        }
        return updates;
    }

    private void importVehicle() {
        String json = editorJson();
        if (json == null) {
            return;
        }
        ImportResult result = services.importService.parse(json);
        if (!result.isValid()) {
            currentResult = result;
            showInvalidStatus(result);
            return;
        }
        currentResult = result;
        BackgroundTasks.run(
                this,
                () -> services.vehicleService.importAllAndSave(currentResult, services.activeProfile),
                saved -> {
                    dispose();
                    onSuccess.accept(saved.isEmpty() ? null : saved.get(0).getId());
                },
                error -> BackgroundTasks.showError(this, "Import Failed", error));
    }
}
