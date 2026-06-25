package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import za.driver.model.VehicleIdentity;
import za.driver.service.AppServices;
import za.driver.spreadsheet.SpreadsheetImportResult;

public class SpreadsheetImportDialog extends JDialog {

    private final AppServices services;
    private final Runnable onSuccess;

    private final JLabel fileLabel = new JLabel("No file selected");
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea detailArea = new JTextArea(12, 60);
    private final JButton previewButton = new JButton("Preview");
    private final JButton importButton = new JButton("Import");
    private final JButton browseButton = new JButton("Browse…");
    private final JButton cancelButton = new JButton("Cancel");

    private Path selectedFile;
    private SpreadsheetImportResult currentResult;

    public SpreadsheetImportDialog(JFrame owner, AppServices services, Runnable onSuccess) {
        super(owner, "Import CSV", true);
        this.services = services;
        this.onSuccess = onSuccess;

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        previewButton.setEnabled(false);
        importButton.setEnabled(false);

        browseButton.addActionListener(e -> browse());
        previewButton.addActionListener(e -> preview());
        importButton.addActionListener(e -> importSpreadsheet());
        cancelButton.addActionListener(e -> dispose());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(browseButton);
        topPanel.add(fileLabel);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
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

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        selectedFile = file.toPath();
        fileLabel.setText(file.getName());
        previewButton.setEnabled(true);
        importButton.setEnabled(false);
        currentResult = null;
        statusLabel.setText(" ");
        detailArea.setText("");
    }

    private void preview() {
        if (selectedFile == null) {
            return;
        }
        BackgroundTasks.run(
                this,
                () -> services.spreadsheetImportService.preview(selectedFile),
                result -> {
                    currentResult = result;
                    if (!result.isValid()) {
                        importButton.setEnabled(false);
                        statusLabel.setText(String.join(" ", result.getErrors()));
                        detailArea.setText(buildDetail(result));
                        return;
                    }
                    statusLabel.setText(buildStatusSummary(result));
                    detailArea.setText(buildDetail(result));
                    importButton.setEnabled(true);
                },
                error -> {
                    currentResult = null;
                    importButton.setEnabled(false);
                    statusLabel.setText("Preview failed: " + error.getMessage());
                    BackgroundTasks.showError(this, "Import Preview", error);
                });
    }

    private void importSpreadsheet() {
        if (currentResult == null || !currentResult.isValid()) {
            preview();
            return;
        }
        BackgroundTasks.run(
                this,
                () -> services.vehicleService.importSpreadsheet(currentResult, services.activeProfile),
                saved -> {
                    dispose();
                    onSuccess.run();
                },
                error -> BackgroundTasks.showError(this, "Import Failed", error));
    }

    private static String buildStatusSummary(SpreadsheetImportResult result) {
        int total = result.getEntries().size();
        int creates = result.createCount();
        int updates = result.updateCount();
        StringBuilder summary = new StringBuilder("Ready to import ").append(total).append(" vehicle(s): ");
        summary.append(creates).append(" new");
        if (updates > 0) {
            summary.append(", ").append(updates).append(" update").append(updates == 1 ? "" : "s");
        }
        return summary.toString();
    }

    private static String buildDetail(SpreadsheetImportResult result) {
        StringBuilder detail = new StringBuilder();
        if (!result.getErrors().isEmpty()) {
            detail.append("Errors:\n");
            for (String error : result.getErrors()) {
                detail.append("- ").append(error).append('\n');
            }
            detail.append('\n');
        }
        appendEntrySection(
                detail,
                "Creates:",
                result.getEntries().stream()
                        .filter(entry -> entry.action() == SpreadsheetImportResult.ImportAction.CREATE)
                        .toList(),
                true);
        appendEntrySection(
                detail,
                "Updates:",
                result.getEntries().stream()
                        .filter(entry -> entry.action() == SpreadsheetImportResult.ImportAction.UPDATE)
                        .toList(),
                false);
        return detail.toString().trim();
    }

    private static void appendEntrySection(
            StringBuilder detail,
            String heading,
            java.util.List<SpreadsheetImportResult.SpreadsheetImportEntry> entries,
            boolean createSection) {
        if (entries.isEmpty()) {
            return;
        }
        detail.append(heading).append('\n');
        for (SpreadsheetImportResult.SpreadsheetImportEntry entry : entries) {
            detail.append("- ");
            if (createSection) {
                detail.append(VehicleIdentity.label(entry.vehicle()))
                        .append(" (")
                        .append(entry.vehicleId())
                        .append(", ")
                        .append(entry.changedFieldCount())
                        .append(" field")
                        .append(entry.changedFieldCount() == 1 ? "" : "s")
                        .append(")");
            } else {
                detail.append(entry.vehicleId())
                        .append(" (")
                        .append(entry.changedFieldCount())
                        .append(" changed field")
                        .append(entry.changedFieldCount() == 1 ? "" : "s")
                        .append(")");
            }
            detail.append('\n');
        }
        detail.append('\n');
    }
}
