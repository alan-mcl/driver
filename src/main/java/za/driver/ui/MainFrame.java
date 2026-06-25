package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import za.driver.model.Vehicle;
import za.driver.model.VehicleIdentity;
import za.driver.model.VehicleStatus;
import za.driver.service.AppServices;

public class MainFrame extends JFrame {

    private static final String APP_NAME = "Driver";
    private static final String APP_VERSION = "1.1";

    private final AppServices services;
    private final VehicleListPanel listPanel;
    private final VehicleDetailPanel detailPanel;
    private final Set<UUID> persistedIds = new HashSet<>();
    private ScatterPlotDialog scatterPlotDialog;
    private UUID lastSelectedId;
    private boolean selectionGuard;

    public MainFrame(AppServices services) {
        super(APP_NAME);
        this.services = services;
        this.listPanel = new VehicleListPanel(services.activeProfile, services.garageDimensions);
        this.detailPanel = new VehicleDetailPanel(
                services.scoringDataReportService,
                services.brandReliabilityConfigService,
                services.activeProfile);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestExit();
            }
        });
        applyWindowIcons();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setJMenuBar(createMenuBar());

        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        newButton.addActionListener(e -> createNewVehicle());
        saveButton.addActionListener(e -> saveVehicle());
        deleteButton.addActionListener(e -> deleteVehicle());
        refreshButton.addActionListener(e -> refreshVehicles());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, detailPanel);
        splitPane.setResizeWeight(0.78);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Vehicles", splitPane);

        JPanel content = new JPanel(new BorderLayout());
        content.add(toolBar, BorderLayout.NORTH);
        content.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(content);

        listPanel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || selectionGuard) {
                return;
            }
            Vehicle selected = listPanel.getSelectedVehicle();
            UUID newId = selected != null ? selected.getId() : null;
            if (Objects.equals(newId, lastSelectedId)) {
                return;
            }
            confirmNavigateAway(
                    () -> {
                        lastSelectedId = newId;
                        if (selected != null) {
                            detailPanel.loadVehicle(selected, persistedIds.contains(selected.getId()));
                        } else {
                            detailPanel.loadVehicle(null, false);
                        }
                    },
                    this::revertListSelection);
        });

        refreshVehicles();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem importItem = new JMenuItem("Import JSON…");
        importItem.setMnemonic(KeyEvent.VK_I);
        importItem.addActionListener(e -> openImportDialog());
        fileMenu.add(importItem);

        JMenuItem exportSpreadsheetItem = new JMenuItem("Export CSV…");
        exportSpreadsheetItem.addActionListener(e -> exportSpreadsheet());
        fileMenu.add(exportSpreadsheetItem);

        JMenuItem exportPresentationItem = new JMenuItem("Export Presentation…");
        exportPresentationItem.addActionListener(e -> exportPresentation());
        fileMenu.add(exportPresentationItem);

        JMenuItem importSpreadsheetItem = new JMenuItem("Import CSV…");
        importSpreadsheetItem.addActionListener(e -> openSpreadsheetImportDialog());
        fileMenu.add(importSpreadsheetItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.addActionListener(e -> requestExit());
        fileMenu.add(exitItem);

        JMenu configMenu = new JMenu("Config");
        configMenu.setMnemonic(KeyEvent.VK_C);

        JMenuItem scoringWeightsItem = new JMenuItem("Scoring Profile…");
        scoringWeightsItem.setMnemonic(KeyEvent.VK_W);
        scoringWeightsItem.addActionListener(e -> openScoringWeightsDialog());
        configMenu.add(scoringWeightsItem);

        JMenuItem garageDimensionsItem = new JMenuItem("Garage Dimensions…");
        garageDimensionsItem.addActionListener(e -> openGarageDimensionsDialog());
        configMenu.add(garageDimensionsItem);

        JMenuItem brandReliabilityItem = new JMenuItem("Brand Reliability…");
        brandReliabilityItem.addActionListener(e -> openBrandReliabilityDialog());
        configMenu.add(brandReliabilityItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem scatterPlotItem = new JMenuItem("Scatter Plot…");
        scatterPlotItem.addActionListener(e -> openScatterPlotDialog());
        viewMenu.add(scatterPlotItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(configMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private static String htmlEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void applyWindowIcons() {
        List<Image> icons = AppBranding.loadWindowIcons();
        if (!icons.isEmpty()) {
            setIconImages(icons);
        }
    }

    private void showAboutDialog() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        ImageIcon logo = AppBranding.loadLogo(480);
        if (logo != null) {
            JLabel logoLabel = new JLabel(logo, JLabel.CENTER);
            panel.add(logoLabel, BorderLayout.NORTH);
        }
        String dataPath = htmlEscape(services.dataRoot.toString());
        JLabel details = new JLabel(
                "<html><center>"
                        + APP_NAME
                        + "<br>Version "
                        + APP_VERSION
                        + "<br><br>Personal decision-support for comparing and shortlisting vehicles."
                        + "<br><br>Data directory:<br>"
                        + "<span style='font-family:monospace;font-size:smaller'>"
                        + dataPath
                        + "</span>"
                        + "</center></html>",
                JLabel.CENTER);
        panel.add(details, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, panel, "About " + APP_NAME, JOptionPane.PLAIN_MESSAGE);
    }

    private void openScoringWeightsDialog() {
        ScoringWeightsDialog dialog = new ScoringWeightsDialog(this, services, ignored -> {
            listPanel.setActiveProfile(services.activeProfile);
            detailPanel.setActiveProfile(services.activeProfile);
            UUID selectedId = listPanel.getSelectedVehicle() != null ? listPanel.getSelectedVehicle().getId() : null;
            BackgroundTasks.run(
                    this,
                    () -> services.vehicleService.findAll(services.activeProfile),
                    vehicles -> {
                        applyVehicleList(vehicles);
                        if (selectedId != null) {
                            listPanel.setSelectedVehicle(selectedId);
                            Vehicle selected = listPanel.getSelectedVehicle();
                            if (selected != null) {
                                detailPanel.loadVehicle(selected, persistedIds.contains(selected.getId()));
                            }
                        }
                    },
                    error -> BackgroundTasks.showError(this, "Load Failed", error));
        });
        dialog.setVisible(true);
    }

    private void openGarageDimensionsDialog() {
        GarageDimensionsDialog dialog = new GarageDimensionsDialog(this, services, saved -> {
            listPanel.setGarageDimensions(saved);
        });
        dialog.setVisible(true);
    }

    private void openBrandReliabilityDialog() {
        BrandReliabilityDialog dialog = new BrandReliabilityDialog(this, services, ignored -> {
            UUID selectedId = listPanel.getSelectedVehicle() != null ? listPanel.getSelectedVehicle().getId() : null;
            BackgroundTasks.run(
                    this,
                    () -> services.vehicleService.findAll(services.activeProfile),
                    vehicles -> {
                        applyVehicleList(vehicles);
                        if (selectedId != null) {
                            listPanel.setSelectedVehicle(selectedId);
                            Vehicle selected = listPanel.getSelectedVehicle();
                            if (selected != null) {
                                detailPanel.loadVehicle(selected, persistedIds.contains(selected.getId()));
                            }
                        }
                    },
                    error -> BackgroundTasks.showError(this, "Load Failed", error));
        });
        dialog.setVisible(true);
    }

    private void openScatterPlotDialog() {
        if (scatterPlotDialog == null) {
            scatterPlotDialog = new ScatterPlotDialog(
                    this,
                    listPanel::getVisibleVehicles,
                    vehicle -> confirmNavigateAway(
                            () -> {
                                setListSelection(vehicle.getId());
                                lastSelectedId = vehicle.getId();
                                detailPanel.loadVehicle(vehicle, persistedIds.contains(vehicle.getId()));
                            },
                            () -> {}));
        } else {
            scatterPlotDialog.refreshData();
        }
        scatterPlotDialog.setVisible(true);
        scatterPlotDialog.toFront();
    }

    private void openImportDialog() {
        ImportDialog dialog = new ImportDialog(this, services, this::refreshAndSelect);
        dialog.setVisible(true);
    }

    private void openSpreadsheetImportDialog() {
        SpreadsheetImportDialog dialog = new SpreadsheetImportDialog(this, services, this::refreshVehicles);
        dialog.setVisible(true);
    }

    private void exportSpreadsheet() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        chooser.setSelectedFile(new java.io.File("driver-vehicles-" + LocalDate.now() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();
        String fileName = file.getName().toLowerCase().endsWith(".csv") ? file.getName() : file.getName() + ".csv";
        java.nio.file.Path outputPath = file.toPath().getParent().resolve(fileName);

        BackgroundTasks.run(
                this,
                () -> {
                    List<Vehicle> vehicles = services.vehicleService.findAll(services.activeProfile);
                    services.spreadsheetExportService.export(outputPath, vehicles);
                    return vehicles.size();
                },
                count -> JOptionPane.showMessageDialog(
                        this,
                        "Exported " + count + " vehicle(s) to:\n" + outputPath,
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE),
                error -> BackgroundTasks.showError(this, "Export Failed", error));
    }

    private void exportPresentation() {
        List<Vehicle> selected = listPanel.getSelectedVehicles();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select one or more vehicles in the list to export a presentation.",
                    "Export Presentation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose folder for presentation export");
        chooser.setSelectedFile(new java.io.File("driver-presentation-" + LocalDate.now()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File chosen = chooser.getSelectedFile();
        Path outputDir = chosen.toPath();

        Set<UUID> selectedIds = selected.stream()
                .map(Vehicle::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        BackgroundTasks.run(
                this,
                () -> {
                    Map<UUID, Vehicle> scoredById = new HashMap<>();
                    for (Vehicle vehicle : services.vehicleService.findAll(services.activeProfile)) {
                        if (vehicle.getId() != null && selectedIds.contains(vehicle.getId())) {
                            scoredById.put(vehicle.getId(), vehicle);
                        }
                    }
                    List<Vehicle> vehiclesToExport = selected.stream()
                            .map(Vehicle::getId)
                            .map(scoredById::get)
                            .filter(Objects::nonNull)
                            .toList();
                    if (vehiclesToExport.isEmpty()) {
                        throw new IllegalStateException("Selected vehicles could not be loaded.");
                    }
                    services.presentationExportService.export(outputDir, vehiclesToExport, services.activeProfile);
                    return outputDir;
                },
                path -> showPresentationExportCompleteDialog(path, selected.size()),
                error -> BackgroundTasks.showError(this, "Export Failed", error));
    }

    private void showPresentationExportCompleteDialog(Path outputDir, int selectedCount) {
        String message = "Exported presentation for "
                + selectedCount
                + " vehicle(s) to:\n"
                + outputDir
                + "\n\nDrop hero images into the images/ folder (see IMAGES.md), then open index.html.";

        Object[] options = {"Open Folder", "Open in Browser", "Close"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Export Complete",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[2]);

        if (choice == 0) {
            openExportedPath(outputDir);
        } else if (choice == 1) {
            openExportedPath(outputDir.resolve("index.html"));
        }
    }

    private void openExportedPath(Path path) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (path.toString().endsWith(".html")) {
                desktop.browse(path.toUri());
            } else {
                desktop.open(path.toFile());
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not open:\n" + path,
                    "Open Failed",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createNewVehicle() {
        confirmNavigateAway(
                () -> {
                    setListSelection(null);
                    lastSelectedId = null;
                    Vehicle vehicle = new Vehicle();
                    vehicle.setId(UUID.randomUUID());
                    vehicle.setStatus(VehicleStatus.CANDIDATE);
                    detailPanel.loadVehicle(vehicle, false);
                },
                () -> {});
    }

    private void saveVehicle() {
        saveVehicleAndThen(null);
    }

    private void saveVehicleAndThen(Runnable onSuccess) {
        if (!detailPanel.validateForSave()) {
            JOptionPane.showMessageDialog(this, "Make and model are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle vehicle = detailPanel.buildVehicle();
        BackgroundTasks.run(
                this,
                () -> services.vehicleService.save(
                        vehicle,
                        services.activeProfile,
                        detailPanel.getScoringOverrides()),
                saved -> {
                    persistedIds.add(saved.getId());
                    refreshVehicles();
                    setListSelection(saved.getId());
                    lastSelectedId = saved.getId();
                    detailPanel.loadVehicle(saved, true);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },
                error -> BackgroundTasks.showError(this, "Save Failed", error));
    }

    private void requestExit() {
        confirmNavigateAway(() -> System.exit(0), () -> {});
    }

    private void confirmNavigateAway(Runnable onProceed, Runnable onCancel) {
        if (!detailPanel.isDirty()) {
            onProceed.run();
            return;
        }

        Object[] options = {"Save", "Don't Save", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "You have unsaved changes. Save before continuing?",
                "Unsaved Changes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            onCancel.run();
            return;
        }
        if (choice == 1) {
            onProceed.run();
            return;
        }
        saveVehicleAndThen(onProceed);
    }

    private void revertListSelection() {
        selectionGuard = true;
        try {
            setListSelection(lastSelectedId);
        } finally {
            selectionGuard = false;
        }
    }

    private void setListSelection(UUID vehicleId) {
        selectionGuard = true;
        try {
            if (vehicleId != null) {
                listPanel.setSelectedVehicle(vehicleId);
            } else {
                listPanel.getTable().clearSelection();
            }
        } finally {
            selectionGuard = false;
        }
    }

    private void deleteVehicle() {
        Vehicle selected = listPanel.getSelectedVehicle();
        if (selected == null || !persistedIds.contains(selected.getId())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a saved vehicle to delete.",
                    "Delete Vehicle",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String label = VehicleIdentity.label(selected);
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + label + "\"?\nThis cannot be undone.",
                "Delete Vehicle",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        UUID vehicleId = selected.getId();
        BackgroundTasks.run(
                this,
                () -> {
                    services.vehicleService.delete(vehicleId);
                    return null;
                },
                ignored -> {
                    detailPanel.loadVehicle(null, false);
                    refreshVehicles();
                },
                error -> BackgroundTasks.showError(this, "Delete Failed", error));
    }

    private void refreshAndSelect(UUID vehicleId) {
        confirmNavigateAway(
                () -> BackgroundTasks.run(
                        this,
                        () -> services.vehicleService.findAll(services.activeProfile),
                        vehicles -> {
                            applyVehicleList(vehicles);
                            if (vehicleId != null) {
                                setListSelection(vehicleId);
                                lastSelectedId = vehicleId;
                                Vehicle selected = listPanel.getSelectedVehicle();
                                if (selected != null) {
                                    detailPanel.loadVehicle(selected, true);
                                }
                            }
                        },
                        error -> BackgroundTasks.showError(this, "Load Failed", error)),
                () -> {});
    }

    private void refreshVehicles() {
        confirmNavigateAway(
                () -> BackgroundTasks.run(
                        this,
                        () -> services.vehicleService.findAll(services.activeProfile),
                        this::applyVehicleList,
                        error -> BackgroundTasks.showError(this, "Load Failed", error)),
                () -> {});
    }

    private void applyVehicleList(List<Vehicle> vehicles) {
        persistedIds.clear();
        UUID selectedId = listPanel.getSelectedVehicle() != null ? listPanel.getSelectedVehicle().getId() : null;
        for (Vehicle vehicle : vehicles) {
            persistedIds.add(vehicle.getId());
        }
        listPanel.loadVehicles(vehicles);
        if (selectedId != null) {
            setListSelection(selectedId);
            lastSelectedId = selectedId;
            Vehicle selected = listPanel.getSelectedVehicle();
            if (selected != null) {
                detailPanel.loadVehicle(selected, persistedIds.contains(selected.getId()));
            }
        } else if (!vehicles.isEmpty()) {
            selectionGuard = true;
            try {
                listPanel.getTable().setRowSelectionInterval(0, 0);
                Vehicle selected = listPanel.getSelectedVehicle();
                lastSelectedId = selected != null ? selected.getId() : null;
                if (selected != null) {
                    detailPanel.loadVehicle(selected, persistedIds.contains(selected.getId()));
                }
            } finally {
                selectionGuard = false;
            }
        } else {
            lastSelectedId = null;
            detailPanel.loadVehicle(null, false);
        }
    }
}
