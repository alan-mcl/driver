package za.driver.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.presentation.MetricLabels;
import za.driver.service.ScoringProfileService;

public class WeightEditor extends JPanel {

    private static final Metric[] BASE_METRIC_ORDER = {
            Metric.SAFETY,
            Metric.RUNNING_COST,
            Metric.RELIABILITY,
            Metric.COMFORT,
            Metric.PERFORMANCE,
            Metric.DAILY_DRIVER,
            Metric.TECHNOLOGY,
            Metric.PRESTIGE
    };

    private static final Dimension SPINNER_SIZE = new Dimension(72, 26);
    private static final int TOP_METRIC_COUNT = 4;

    private final JTextField profileNameField = new JTextField(24);
    private final JTextField aggregateNameField = new JTextField(16);
    private final JSpinner aggregateWeightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JLabel profileTotalLabel = new JLabel();
    private final JLabel componentTotalLabel = new JLabel();
    private final JLabel topCountLabel = new JLabel();
    private final Map<Metric, JCheckBox> topCheckboxes = new EnumMap<>(Metric.class);
    private final Map<Metric, JSpinner> weightSpinners = new EnumMap<>(Metric.class);
    private final ChangeListener validationUpdater = e -> {
        updateValidationLabels();
        notifyValidationListeners();
    };
    private final java.util.List<Runnable> validationListeners = new ArrayList<>();
    private boolean suppressCheckboxEvents;

    public WeightEditor() {
        super(new GridBagLayout());
        aggregateWeightSpinner.setPreferredSize(SPINNER_SIZE);
        aggregateWeightSpinner.addChangeListener(validationUpdater);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 8, 0);
        add(new JLabel("Profile name:"), constraints);

        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 12, 0);
        add(profileNameField, constraints);

        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(4, 0, 4, 8);
        constraints.gridy = 2;
        constraints.gridx = 0;
        add(new JLabel("Top"), constraints);
        constraints.gridx = 1;
        add(new JLabel("Metric"), constraints);
        constraints.gridx = 2;
        add(new JLabel("Weight"), constraints);

        int row = 3;
        for (Metric metric : BASE_METRIC_ORDER) {
            GridBagConstraints checkboxConstraints = new GridBagConstraints();
            checkboxConstraints.gridx = 0;
            checkboxConstraints.gridy = row;
            checkboxConstraints.anchor = GridBagConstraints.WEST;
            checkboxConstraints.insets = new Insets(4, 0, 4, 8);

            JCheckBox topCheckbox = new JCheckBox();
            topCheckbox.addActionListener(e -> onTopSelectionChanged(metric));
            topCheckboxes.put(metric, topCheckbox);
            add(topCheckbox, checkboxConstraints);

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 1;
            labelConstraints.gridy = row;
            labelConstraints.anchor = GridBagConstraints.WEST;
            labelConstraints.insets = new Insets(4, 0, 4, 12);
            add(new JLabel(MetricLabels.displayName(metric)), labelConstraints);

            GridBagConstraints spinnerConstraints = new GridBagConstraints();
            spinnerConstraints.gridx = 2;
            spinnerConstraints.gridy = row;
            spinnerConstraints.anchor = GridBagConstraints.WEST;
            spinnerConstraints.insets = new Insets(4, 0, 4, 0);

            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
            spinner.setPreferredSize(SPINNER_SIZE);
            spinner.addChangeListener(validationUpdater);
            weightSpinners.put(metric, spinner);
            add(spinner, spinnerConstraints);
            row++;
        }

        GridBagConstraints separatorConstraints = new GridBagConstraints();
        separatorConstraints.gridx = 0;
        separatorConstraints.gridy = row;
        separatorConstraints.gridwidth = 3;
        separatorConstraints.fill = GridBagConstraints.HORIZONTAL;
        separatorConstraints.insets = new Insets(8, 0, 8, 0);
        add(new JSeparator(), separatorConstraints);
        row++;

        GridBagConstraints aggregateLabelConstraints = new GridBagConstraints();
        aggregateLabelConstraints.gridx = 0;
        aggregateLabelConstraints.gridy = row;
        aggregateLabelConstraints.gridwidth = 2;
        aggregateLabelConstraints.anchor = GridBagConstraints.WEST;
        aggregateLabelConstraints.insets = new Insets(4, 0, 4, 8);
        add(new JLabel("Aggregate name"), aggregateLabelConstraints);

        GridBagConstraints aggregateNameConstraints = new GridBagConstraints();
        aggregateNameConstraints.gridx = 0;
        aggregateNameConstraints.gridy = row + 1;
        aggregateNameConstraints.gridwidth = 2;
        aggregateNameConstraints.fill = GridBagConstraints.HORIZONTAL;
        aggregateNameConstraints.insets = new Insets(0, 0, 8, 8);
        add(aggregateNameField, aggregateNameConstraints);

        GridBagConstraints aggregateWeightLabelConstraints = new GridBagConstraints();
        aggregateWeightLabelConstraints.gridx = 2;
        aggregateWeightLabelConstraints.gridy = row;
        aggregateWeightLabelConstraints.anchor = GridBagConstraints.WEST;
        aggregateWeightLabelConstraints.insets = new Insets(4, 0, 4, 0);
        add(new JLabel("Profile weight"), aggregateWeightLabelConstraints);

        GridBagConstraints aggregateWeightConstraints = new GridBagConstraints();
        aggregateWeightConstraints.gridx = 2;
        aggregateWeightConstraints.gridy = row + 1;
        aggregateWeightConstraints.anchor = GridBagConstraints.WEST;
        aggregateWeightConstraints.insets = new Insets(0, 0, 8, 0);
        add(aggregateWeightSpinner, aggregateWeightConstraints);
        row += 2;

        GridBagConstraints summaryConstraints = new GridBagConstraints();
        summaryConstraints.gridx = 0;
        summaryConstraints.gridy = row;
        summaryConstraints.gridwidth = 3;
        summaryConstraints.anchor = GridBagConstraints.WEST;
        summaryConstraints.insets = new Insets(8, 0, 4, 0);
        add(topCountLabel, summaryConstraints);
        row++;

        summaryConstraints.gridy = row;
        add(profileTotalLabel, summaryConstraints);
        row++;

        summaryConstraints.gridy = row;
        add(componentTotalLabel, summaryConstraints);

        profileNameField.getDocument().addDocumentListener(documentValidationListener());
        aggregateNameField.getDocument().addDocumentListener(documentValidationListener());

        updateValidationLabels();
    }

    public void loadFrom(ScoringProfile profile) {
        suppressCheckboxEvents = true;
        try {
            profileNameField.setText(profile != null && profile.getName() != null ? profile.getName() : "Profile");
            aggregateNameField.setText(
                    profile != null && profile.getAggregateName() != null && !profile.getAggregateName().isBlank()
                            ? profile.getAggregateName()
                            : "Awesomeness");

            Map<Metric, Double> topWeights = new EnumMap<>(Metric.class);
            Map<Metric, Double> componentWeights = new EnumMap<>(Metric.class);
            double aggregateWeight = 0.0;

            if (profile != null && profile.getWeights() != null) {
                for (ScoringWeight scoringWeight : profile.getWeights()) {
                    if (scoringWeight.getMetric() == null || scoringWeight.getWeight() == null) {
                        continue;
                    }
                    if (scoringWeight.getMetric() == Metric.AWESOMENESS) {
                        aggregateWeight = scoringWeight.getWeight();
                    } else {
                        topWeights.put(scoringWeight.getMetric(), scoringWeight.getWeight());
                    }
                }
            }

            if (profile != null && profile.getAggregateComponents() != null) {
                for (ScoringWeight scoringWeight : profile.getAggregateComponents()) {
                    if (scoringWeight.getMetric() != null && scoringWeight.getWeight() != null) {
                        componentWeights.put(scoringWeight.getMetric(), scoringWeight.getWeight());
                    }
                }
            }

            Set<Metric> topMetrics = topWeights.keySet();
            if (componentWeights.isEmpty() && !topMetrics.isEmpty()) {
                for (Metric metric : ScoringProfileService.complementTopMetrics(topMetrics)) {
                    componentWeights.put(metric, defaultComponentWeight(metric));
                }
            }

            for (Metric metric : BASE_METRIC_ORDER) {
                boolean isTop = topMetrics.contains(metric);
                topCheckboxes.get(metric).setSelected(isTop);
                Double weight = isTop ? topWeights.get(metric) : componentWeights.get(metric);
                weightSpinners.get(metric).setValue(weight != null ? (int) weight.doubleValue() : 0);
            }

            aggregateWeightSpinner.setValue((int) aggregateWeight);
        } finally {
            suppressCheckboxEvents = false;
        }
        updateValidationLabels();
    }

    public List<ScoringWeight> buildWeights() {
        List<ScoringWeight> weights = new ArrayList<>();
        for (Metric metric : BASE_METRIC_ORDER) {
            if (topCheckboxes.get(metric).isSelected()) {
                weights.add(weight(metric, spinnerValue(metric)));
            }
        }
        weights.add(weight(Metric.AWESOMENESS, (Integer) aggregateWeightSpinner.getValue()));
        return weights;
    }

    public List<ScoringWeight> buildAggregateComponents() {
        List<ScoringWeight> components = new ArrayList<>();
        for (Metric metric : BASE_METRIC_ORDER) {
            if (!topCheckboxes.get(metric).isSelected()) {
                components.add(weight(metric, spinnerValue(metric)));
            }
        }
        return components;
    }

    public String getProfileName() {
        return profileNameField.getText();
    }

    public String getAggregateName() {
        return aggregateNameField.getText();
    }

    public boolean isConfigurationValid() {
        return topMetricCount() == TOP_METRIC_COUNT
                && profileTotal() == 100
                && componentTotal() == 100
                && getProfileName() != null
                && !getProfileName().isBlank()
                && getAggregateName() != null
                && !getAggregateName().isBlank();
    }

    public void addValidationListener(Runnable listener) {
        validationListeners.add(listener);
    }

    public void addWeightChangeListener(ChangeListener listener) {
        for (JSpinner spinner : weightSpinners.values()) {
            spinner.addChangeListener(listener);
        }
        aggregateWeightSpinner.addChangeListener(listener);
    }

    static String displayName(Metric metric) {
        return MetricLabels.displayName(metric);
    }

    private void onTopSelectionChanged(Metric metric) {
        if (suppressCheckboxEvents) {
            return;
        }

        JCheckBox checkbox = topCheckboxes.get(metric);
        if (checkbox.isSelected() && topMetricCount() > TOP_METRIC_COUNT) {
            suppressCheckboxEvents = true;
            checkbox.setSelected(false);
            suppressCheckboxEvents = false;
        }
        updateValidationLabels();
        notifyValidationListeners();
    }

    private void notifyValidationListeners() {
        for (Runnable listener : validationListeners) {
            listener.run();
        }
    }

    private javax.swing.event.DocumentListener documentValidationListener() {
        return new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateValidationLabels();
                notifyValidationListeners();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateValidationLabels();
                notifyValidationListeners();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateValidationLabels();
                notifyValidationListeners();
            }
        };
    }

    private int topMetricCount() {
        if (topCheckboxes == null || topCheckboxes.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (JCheckBox checkbox : topCheckboxes.values()) {
            if (checkbox.isSelected()) {
                count++;
            }
        }
        return count;
    }

    private int profileTotal() {
        if (topCheckboxes.isEmpty() || weightSpinners.isEmpty()) {
            return 0;
        }
        int total = (Integer) aggregateWeightSpinner.getValue();
        for (Metric metric : BASE_METRIC_ORDER) {
            if (topCheckboxes.get(metric).isSelected()) {
                total += spinnerValue(metric);
            }
        }
        return total;
    }

    private int componentTotal() {
        if (topCheckboxes.isEmpty() || weightSpinners.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Metric metric : BASE_METRIC_ORDER) {
            if (!topCheckboxes.get(metric).isSelected()) {
                total += spinnerValue(metric);
            }
        }
        return total;
    }

    private int spinnerValue(Metric metric) {
        return (Integer) weightSpinners.get(metric).getValue();
    }

    private void updateValidationLabels() {
        int topCount = topMetricCount();
        topCountLabel.setText("Top metrics selected: " + topCount + " / " + TOP_METRIC_COUNT);
        topCountLabel.setForeground(topCount == TOP_METRIC_COUNT ? new Color(0, 128, 0) : Color.RED);

        int profileTotal = profileTotal();
        profileTotalLabel.setText("Profile total: " + profileTotal + " / 100");
        profileTotalLabel.setForeground(profileTotal == 100 ? new Color(0, 128, 0) : Color.RED);

        int componentTotal = componentTotal();
        componentTotalLabel.setText("Aggregate composition total: " + componentTotal + " / 100");
        componentTotalLabel.setForeground(componentTotal == 100 ? new Color(0, 128, 0) : Color.RED);
    }

    private static double defaultComponentWeight(Metric metric) {
        return switch (metric) {
            case PRESTIGE -> 55.0;
            case COMFORT, DAILY_DRIVER, TECHNOLOGY -> 15.0;
            default -> 0.0;
        };
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
