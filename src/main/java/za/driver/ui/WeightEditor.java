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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;

public class WeightEditor extends JPanel {

    private static final Metric[] METRIC_ORDER = {
            Metric.SAFETY,
            Metric.RUNNING_COST,
            Metric.RELIABILITY,
            Metric.PERFORMANCE,
            Metric.AWESOMENESS
    };

    private static final Dimension SPINNER_SIZE = new Dimension(72, 26);

    private final JLabel profileNameLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();
    private final Map<Metric, JSpinner> spinners = new EnumMap<>(Metric.class);
    private final ChangeListener totalUpdater = e -> updateTotalLabel();

    public WeightEditor() {
        super(new GridBagLayout());
        profileNameLabel.setFont(profileNameLabel.getFont().deriveFont(java.awt.Font.BOLD));

        GridBagConstraints header = new GridBagConstraints();
        header.gridx = 0;
        header.gridy = 0;
        header.gridwidth = 2;
        header.anchor = GridBagConstraints.WEST;
        header.insets = new Insets(0, 0, 12, 0);
        add(profileNameLabel, header);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(4, 0, 4, 12);

        GridBagConstraints spinnerConstraints = new GridBagConstraints();
        spinnerConstraints.gridx = 1;
        spinnerConstraints.anchor = GridBagConstraints.WEST;
        spinnerConstraints.insets = new Insets(4, 0, 4, 0);

        int row = 1;
        for (Metric metric : METRIC_ORDER) {
            if (metric == Metric.AWESOMENESS) {
                GridBagConstraints separatorConstraints = new GridBagConstraints();
                separatorConstraints.gridx = 0;
                separatorConstraints.gridy = row;
                separatorConstraints.gridwidth = 2;
                separatorConstraints.fill = GridBagConstraints.HORIZONTAL;
                separatorConstraints.insets = new Insets(8, 0, 8, 0);
                add(new JSeparator(), separatorConstraints);
                row++;
            }

            labelConstraints.gridy = row;
            add(new JLabel(displayName(metric)), labelConstraints);

            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
            spinner.setPreferredSize(SPINNER_SIZE);
            spinner.addChangeListener(totalUpdater);
            spinners.put(metric, spinner);

            spinnerConstraints.gridy = row;
            add(spinner, spinnerConstraints);
            row++;

            if (metric == Metric.AWESOMENESS) {
                JLabel helper = new JLabel("Prestige 55%, Comfort 15%, Daily Driver 15%, Technology 15%");
                helper.setForeground(Color.GRAY);
                helper.setFont(helper.getFont().deriveFont(helper.getFont().getSize2D() - 1f));
                GridBagConstraints helperConstraints = new GridBagConstraints();
                helperConstraints.gridx = 0;
                helperConstraints.gridy = row;
                helperConstraints.gridwidth = 2;
                helperConstraints.anchor = GridBagConstraints.WEST;
                helperConstraints.insets = new Insets(0, 0, 4, 0);
                add(helper, helperConstraints);
                row++;
            }
        }

        GridBagConstraints totalConstraints = new GridBagConstraints();
        totalConstraints.gridx = 0;
        totalConstraints.gridy = row;
        totalConstraints.gridwidth = 2;
        totalConstraints.anchor = GridBagConstraints.WEST;
        totalConstraints.insets = new Insets(16, 0, 0, 0);
        add(totalLabel, totalConstraints);

        updateTotalLabel();
    }

    public void loadFrom(ScoringProfile profile) {
        profileNameLabel.setText(profile != null && profile.getName() != null ? profile.getName() : "Profile");

        Map<Metric, Double> weightByMetric = new EnumMap<>(Metric.class);
        if (profile != null && profile.getWeights() != null) {
            for (ScoringWeight scoringWeight : profile.getWeights()) {
                if (scoringWeight.getMetric() != null && scoringWeight.getWeight() != null) {
                    weightByMetric.put(scoringWeight.getMetric(), scoringWeight.getWeight());
                }
            }
        }

        for (Metric metric : METRIC_ORDER) {
            Double weight = weightByMetric.get(metric);
            JSpinner spinner = spinners.get(metric);
            spinner.setValue(weight != null ? (int) weight.doubleValue() : 0);
        }
        updateTotalLabel();
    }

    public List<ScoringWeight> buildWeights() {
        List<ScoringWeight> weights = new ArrayList<>();
        for (Metric metric : METRIC_ORDER) {
            ScoringWeight scoringWeight = new ScoringWeight();
            scoringWeight.setMetric(metric);
            scoringWeight.setWeight((double) (Integer) spinners.get(metric).getValue());
            weights.add(scoringWeight);
        }
        return weights;
    }

    public boolean weightsSumToHundred() {
        return totalWeight() == 100;
    }

    public void addWeightChangeListener(ChangeListener listener) {
        for (JSpinner spinner : spinners.values()) {
            spinner.addChangeListener(listener);
        }
    }

    private int totalWeight() {
        int total = 0;
        for (JSpinner spinner : spinners.values()) {
            total += (Integer) spinner.getValue();
        }
        return total;
    }

    private void updateTotalLabel() {
        int total = totalWeight();
        totalLabel.setText("Total: " + total + " / 100");
        totalLabel.setForeground(total == 100 ? new Color(0, 128, 0) : Color.RED);
    }

    static String displayName(Metric metric) {
        return switch (metric) {
            case SAFETY -> "Safety";
            case RUNNING_COST -> "Running Cost";
            case RELIABILITY -> "Reliability";
            case COMFORT -> "Comfort";
            case PERFORMANCE -> "Performance";
            case DAILY_DRIVER -> "Daily Driver";
            case TECHNOLOGY -> "Technology";
            case PRESTIGE -> "Prestige";
            case AWESOMENESS -> "Awesomeness";
        };
    }
}
