package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import za.driver.model.BodyType;
import za.driver.model.FuelType;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.service.VehicleFilterCriteria;

public class FilterBar extends JPanel {

    private static final String ANY = "Any";
    private static final int MIN_PRICE_ZAR = 10_000;
    private static final int DEFAULT_MAX_PRICE_ZAR = 2_000_000;

    private final JSlider maxPriceSlider = new JSlider(
            MIN_PRICE_ZAR,
            DEFAULT_MAX_PRICE_ZAR,
            DEFAULT_MAX_PRICE_ZAR);
    private final JComboBox<String> bodyTypeCombo = new JComboBox<>();
    private final JComboBox<String> fuelTypeCombo = new JComboBox<>();
    private final JSpinner minSafetySpinner = scoreSpinner();
    private final JSpinner minRunningCostSpinner = scoreSpinner();
    private final JSpinner minReliabilitySpinner = scoreSpinner();
    private final JSpinner minAwesomenessSpinner = scoreSpinner();
    private final JSpinner minOverallSpinner = scoreSpinner();
    private final JSpinner maxWidthSpinner = dimensionSpinner();
    private final JSpinner maxHeightSpinner = dimensionSpinner();
    private final JSpinner minGarageClearanceSpinner = dimensionSpinner();
    private final JComboBox<String> statusCombo = new JComboBox<>();

    private final List<Consumer<VehicleFilterCriteria>> listeners = new ArrayList<>();

    public FilterBar() {
        super(new BorderLayout(0, 4));

        populateEnumCombo(bodyTypeCombo, BodyType.values());
        populateEnumCombo(fuelTypeCombo, FuelType.values());
        populateEnumCombo(statusCombo, VehicleStatus.values());

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel priceCaption = new JLabel("Max price");
        maxPriceSlider.setPreferredSize(new Dimension(200, 36));
        maxPriceSlider.addChangeListener(e -> notifyListeners());
        pricePanel.add(priceCaption);
        pricePanel.add(maxPriceSlider);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(pricePanel);
        row1.add(new JLabel("Body"));
        row1.add(bodyTypeCombo);
        row1.add(new JLabel("Fuel"));
        row1.add(fuelTypeCombo);
        row1.add(new JLabel("Min Safety"));
        row1.add(minSafetySpinner);
        row1.add(new JLabel("Min Run Cost"));
        row1.add(minRunningCostSpinner);
        row1.add(new JLabel("Min Reliability"));
        row1.add(minReliabilitySpinner);
        row1.add(new JLabel("Min Awesomeness"));
        row1.add(minAwesomenessSpinner);
        row1.add(new JLabel("Min Overall"));
        row1.add(minOverallSpinner);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Max width (mm)"));
        row2.add(maxWidthSpinner);
        row2.add(new JLabel("Max height (mm)"));
        row2.add(maxHeightSpinner);
        row2.add(new JLabel("Min clearance (mm)"));
        row2.add(minGarageClearanceSpinner);
        row2.add(new JLabel("Status"));
        row2.add(statusCombo);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(this::clear);
        row2.add(clearButton);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(row1);
        rows.add(row2);
        add(rows, BorderLayout.CENTER);

        ChangeListener changeListener = e -> notifyListeners();
        bodyTypeCombo.addActionListener(e -> notifyListeners());
        fuelTypeCombo.addActionListener(e -> notifyListeners());
        statusCombo.addActionListener(e -> notifyListeners());
        minSafetySpinner.addChangeListener(changeListener);
        minRunningCostSpinner.addChangeListener(changeListener);
        minReliabilitySpinner.addChangeListener(changeListener);
        minAwesomenessSpinner.addChangeListener(changeListener);
        minOverallSpinner.addChangeListener(changeListener);
        maxWidthSpinner.addChangeListener(changeListener);
        maxHeightSpinner.addChangeListener(changeListener);
        minGarageClearanceSpinner.addChangeListener(changeListener);
    }

    public void addChangeListener(Consumer<VehicleFilterCriteria> listener) {
        listeners.add(listener);
    }

    public void updateFleetPrices(List<Vehicle> vehicles) {
        int fleetMaxZar = DEFAULT_MAX_PRICE_ZAR;
        for (Vehicle vehicle : vehicles) {
            Pricing pricing = vehicle.getPricing();
            if (pricing == null || pricing.getPriceZar() == null) {
                continue;
            }
            int priceZar = pricing.getPriceZar().intValue();
            if (priceZar > fleetMaxZar) {
                fleetMaxZar = priceZar;
            }
        }

        int currentValue = maxPriceSlider.getValue();
        maxPriceSlider.setMaximum(Math.max(DEFAULT_MAX_PRICE_ZAR, fleetMaxZar));
        if (currentValue > maxPriceSlider.getMaximum()) {
            maxPriceSlider.setValue(maxPriceSlider.getMaximum());
        }
    }

    public VehicleFilterCriteria currentCriteria() {
        return new VehicleFilterCriteria(
                null,
                sliderMaxPrice(),
                selectedEnum(bodyTypeCombo, BodyType.class),
                selectedEnum(fuelTypeCombo, FuelType.class),
                spinnerScore(minSafetySpinner),
                spinnerScore(minRunningCostSpinner),
                spinnerScore(minReliabilitySpinner),
                spinnerScore(minAwesomenessSpinner),
                spinnerScore(minOverallSpinner),
                spinnerDimension(maxWidthSpinner),
                spinnerDimension(maxHeightSpinner),
                spinnerDimension(minGarageClearanceSpinner),
                selectedEnum(statusCombo, VehicleStatus.class));
    }

    private void clear(ActionEvent event) {
        maxPriceSlider.setValue(maxPriceSlider.getMaximum());
        bodyTypeCombo.setSelectedIndex(0);
        fuelTypeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        minSafetySpinner.setValue(0);
        minRunningCostSpinner.setValue(0);
        minReliabilitySpinner.setValue(0);
        minAwesomenessSpinner.setValue(0);
        minOverallSpinner.setValue(0);
        maxWidthSpinner.setValue(0);
        maxHeightSpinner.setValue(0);
        minGarageClearanceSpinner.setValue(0);
        notifyListeners();
    }

    private BigDecimal sliderMaxPrice() {
        int value = maxPriceSlider.getValue();
        int maximum = maxPriceSlider.getMaximum();
        if (value >= maximum) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    private void notifyListeners() {
        VehicleFilterCriteria criteria = currentCriteria();
        for (Consumer<VehicleFilterCriteria> listener : listeners) {
            listener.accept(criteria);
        }
    }

    private static JSpinner scoreSpinner() {
        return new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    }

    private static JSpinner dimensionSpinner() {
        return new JSpinner(new SpinnerNumberModel(0, 0, 10_000, 1));
    }

    private static void populateEnumCombo(JComboBox<String> combo, Enum<?>[] values) {
        combo.addItem(ANY);
        for (Enum<?> value : values) {
            combo.addItem(value.name());
        }
    }

    private static Double spinnerScore(JSpinner spinner) {
        int value = (Integer) spinner.getValue();
        return value <= 0 ? null : (double) value;
    }

    private static Integer spinnerDimension(JSpinner spinner) {
        Number value = (Number) spinner.getValue();
        int intValue = value.intValue();
        return intValue <= 0 ? null : intValue;
    }

    private static <E extends Enum<E>> E selectedEnum(JComboBox<String> combo, Class<E> enumType) {
        Object selected = combo.getSelectedItem();
        if (selected == null || ANY.equals(selected)) {
            return null;
        }
        return Enum.valueOf(enumType, selected.toString());
    }
}
