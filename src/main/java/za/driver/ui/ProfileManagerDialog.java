package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.service.AppServices;
import za.driver.service.DefaultProfileSeeder;

public class ProfileManagerDialog extends JDialog {

    private final AppServices services;
    private final Consumer<Void> onSuccess;
    private final DefaultListModel<ScoringProfile> listModel = new DefaultListModel<>();
    private final JList<ScoringProfile> profileList = new JList<>(listModel);
    private final WeightEditor weightEditor = new WeightEditor();
    private final JButton saveButton = new JButton("Save");
    private final JButton newButton = new JButton("New");
    private final JButton duplicateButton = new JButton("Duplicate");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton closeButton = new JButton("Close");

    private ScoringProfile editingProfile;
    private boolean creatingNew;
    private boolean listSelectionGuard;

    public ProfileManagerDialog(JFrame owner, AppServices services, Consumer<Void> onSuccess) {
        super(owner, "Manage Scoring Profiles", true);
        this.services = services;
        this.onSuccess = onSuccess;

        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value != null && value.getName() != null ? value.getName() : "Profile");
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });
        profileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() || listSelectionGuard) {
                    return;
                }
                ScoringProfile selected = profileList.getSelectedValue();
                if (selected != null) {
                    creatingNew = false;
                    editingProfile = selected;
                    weightEditor.loadFrom(selected);
                    updateSaveButtonState();
                }
            }
        });

        saveButton.addActionListener(e -> save());
        newButton.addActionListener(e -> startNewProfile());
        duplicateButton.addActionListener(e -> duplicateSelectedProfile());
        deleteButton.addActionListener(e -> deleteSelectedProfile());
        closeButton.addActionListener(e -> dispose());

        weightEditor.addValidationListener(this::updateSaveButtonState);
        weightEditor.addWeightChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSaveButtonState();
            }
        });

        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        listButtons.add(newButton);
        listButtons.add(duplicateButton);
        listButtons.add(deleteButton);

        JPanel listPanel = new JPanel(new BorderLayout(8, 8));
        listPanel.add(new JScrollPane(profileList), BorderLayout.CENTER);
        listPanel.add(listButtons, BorderLayout.SOUTH);
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(weightEditor, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, editorPanel);
        splitPane.setResizeWeight(0.28);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtons.add(saveButton);
        bottomButtons.add(closeButton);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(splitPane, BorderLayout.CENTER);
        content.add(bottomButtons, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(owner);
        reloadProfiles();
    }

    private void reloadProfiles() {
        BackgroundTasks.run(
                this,
                () -> services.profileService.findAll(),
                profiles -> {
                    UUID activeId = services.activeProfile.getId();
                    ScoringProfile selected = editingProfile;
                    if (selected != null && selected.getId() != null) {
                        UUID selectedId = selected.getId();
                        selected = profiles.stream()
                                .filter(p -> selectedId.equals(p.getId()))
                                .findFirst()
                                .orElse(selected);
                    } else if (activeId != null) {
                        selected = profiles.stream()
                                .filter(p -> activeId.equals(p.getId()))
                                .findFirst()
                                .orElse(profiles.isEmpty() ? null : profiles.get(0));
                    }

                    listModel.clear();
                    for (ScoringProfile profile : profiles) {
                        listModel.addElement(profile);
                    }

                    if (creatingNew) {
                        weightEditor.loadFrom(DefaultProfileSeeder.newProfileTemplate());
                        updateSaveButtonState();
                        return;
                    }

                    if (selected != null) {
                        selectProfile(selected);
                    } else if (!profiles.isEmpty()) {
                        selectProfile(profiles.get(0));
                    }
                },
                error -> BackgroundTasks.showError(this, "Load Failed", error));
    }

    private void selectProfile(ScoringProfile profile) {
        listSelectionGuard = true;
        try {
            profileList.setSelectedValue(profile, true);
            creatingNew = false;
            editingProfile = profile;
            weightEditor.loadFrom(profile);
            updateSaveButtonState();
        } finally {
            listSelectionGuard = false;
        }
    }

    private void startNewProfile() {
        listSelectionGuard = true;
        try {
            profileList.clearSelection();
        } finally {
            listSelectionGuard = false;
        }
        creatingNew = true;
        editingProfile = null;
        weightEditor.loadFrom(DefaultProfileSeeder.newProfileTemplate());
        updateSaveButtonState();
    }

    private void duplicateSelectedProfile() {
        ScoringProfile selected = profileList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a profile to duplicate.",
                    "Duplicate Profile",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ScoringProfile copy = new ScoringProfile();
        copy.setName(selected.getName() + " (copy)");
        copy.setAggregateName(selected.getAggregateName());
        copy.setWeights(copyWeights(selected.getWeights()));
        copy.setAggregateComponents(copyWeights(selected.getAggregateComponents()));

        listSelectionGuard = true;
        try {
            profileList.clearSelection();
        } finally {
            listSelectionGuard = false;
        }
        creatingNew = true;
        editingProfile = null;
        weightEditor.loadFrom(copy);
        updateSaveButtonState();
    }

    private void deleteSelectedProfile() {
        ScoringProfile selected = profileList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a profile to delete.",
                    "Delete Profile",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete profile \"" + selected.getName() + "\"?\nThis cannot be undone.",
                "Delete Profile",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        BackgroundTasks.run(
                this,
                () -> {
                    UUID deletedId = selected.getId();
                    services.profileService.deleteProfile(deletedId);
                    if (Objects.equals(deletedId, services.activeProfile.getId())) {
                        List<ScoringProfile> remaining = services.profileService.findAll();
                        if (!remaining.isEmpty()) {
                            services.setActiveProfile(remaining.get(0));
                        }
                    }
                    return null;
                },
                ignored -> {
                    creatingNew = false;
                    editingProfile = null;
                    onSuccess.accept(null);
                    reloadProfiles();
                },
                error -> {
                    if (error instanceof IllegalArgumentException) {
                        JOptionPane.showMessageDialog(
                                this,
                                error.getMessage(),
                                "Delete Profile",
                                JOptionPane.WARNING_MESSAGE);
                    } else if (error instanceof IOException) {
                        BackgroundTasks.showError(this, "Delete Failed", error);
                    } else {
                        BackgroundTasks.showError(this, "Delete Failed", error);
                    }
                });
    }

    private void save() {
        if (!weightEditor.isConfigurationValid()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select exactly four top metrics and ensure both weight totals equal 100.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = weightEditor.getProfileName();
        List<ScoringWeight> weights = weightEditor.buildWeights();
        String aggregateName = weightEditor.getAggregateName();
        List<ScoringWeight> aggregateComponents = weightEditor.buildAggregateComponents();
        ScoringProfile profileBeingEdited = editingProfile;
        boolean creating = creatingNew;

        BackgroundTasks.run(
                this,
                () -> {
                    ScoringProfile saved;
                    if (creating || profileBeingEdited == null || profileBeingEdited.getId() == null) {
                        saved = services.profileService.createProfile(
                                name, weights, aggregateName, aggregateComponents);
                    } else if (Objects.equals(profileBeingEdited.getId(), services.activeProfile.getId())) {
                        services.profileService.updateProfileAndRecalculateAll(
                                profileBeingEdited, name, weights, aggregateName, aggregateComponents);
                        saved = profileBeingEdited;
                    } else {
                        services.profileService.saveProfileEdits(
                                profileBeingEdited, name, weights, aggregateName, aggregateComponents);
                        saved = profileBeingEdited;
                    }
                    return saved;
                },
                saved -> {
                    creatingNew = false;
                    editingProfile = saved;
                    onSuccess.accept(null);
                    reloadProfiles();
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
                        BackgroundTasks.showError(this, "Save Failed", error);
                    }
                });
    }

    private void updateSaveButtonState() {
        saveButton.setEnabled(weightEditor.isConfigurationValid());
    }

    private static List<za.driver.model.ScoringWeight> copyWeights(List<za.driver.model.ScoringWeight> source) {
        List<za.driver.model.ScoringWeight> copies = new ArrayList<>();
        if (source == null) {
            return copies;
        }
        for (za.driver.model.ScoringWeight scoringWeight : source) {
            za.driver.model.ScoringWeight copy = new za.driver.model.ScoringWeight();
            copy.setMetric(scoringWeight.getMetric());
            copy.setWeight(scoringWeight.getWeight());
            copies.add(copy);
        }
        return copies;
    }
}
