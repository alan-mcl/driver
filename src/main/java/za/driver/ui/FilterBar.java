package za.driver.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
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
    private static final int PRICE_SLIDER_INCREMENT_ZAR = 10_000;
    private static final int PRICE_VALUE_LABEL_WIDTH_PX = 110;
    private static final int PRICE_VALUE_LABEL_HEIGHT_PX = 36;
    private static final int PRICE_SLIDER_WIDTH_PX = 140;

    private final JSlider maxPriceSlider = new JSlider(
            MIN_PRICE_ZAR,
            DEFAULT_MAX_PRICE_ZAR,
            DEFAULT_MAX_PRICE_ZAR);
    private final JLabel priceValueLabel = new JLabel("", SwingConstants.RIGHT);
    private final JComboBox<String> bodyTypeCombo = new JComboBox<>();
    private final JComboBox<String> fuelTypeCombo = new JComboBox<>();
    private final JComboBox<String> statusCombo = new JComboBox<>();
    private final JSpinner minOverallSpinner = scoreSpinner();
    private final JSpinner minGarageClearanceSpinner = dimensionSpinner();

    private final List<Consumer<VehicleFilterCriteria>> listeners = new ArrayList<>();

    public FilterBar() {
        super(new BorderLayout(0, 4));

        populateEnumCombo(bodyTypeCombo, BodyType.values());
        populateEnumCombo(fuelTypeCombo, FuelType.values());
        populateEnumCombo(statusCombo, VehicleStatus.values());

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        maxPriceSlider.setPreferredSize(new Dimension(PRICE_SLIDER_WIDTH_PX, PRICE_VALUE_LABEL_HEIGHT_PX));
        Dimension priceLabelSize = new Dimension(PRICE_VALUE_LABEL_WIDTH_PX, PRICE_VALUE_LABEL_HEIGHT_PX);
        priceValueLabel.setPreferredSize(priceLabelSize);
        priceValueLabel.setMinimumSize(priceLabelSize);
        priceValueLabel.setMaximumSize(priceLabelSize);
        maxPriceSlider.addChangeListener(e -> {
            updatePriceValueLabel();
            notifyListeners();
            if (!maxPriceSlider.getValueIsAdjusting()) {
                snapMaxPriceSliderToIncrement();
            }
        });
        configurePriceSliderKeyboard();
        pricePanel.add(priceValueLabel);
        pricePanel.add(maxPriceSlider);
        updatePriceValueLabel();

        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtersRow.add(pricePanel);
        filtersRow.add(new JLabel("Body"));
        filtersRow.add(bodyTypeCombo);
        filtersRow.add(new JLabel("Fuel"));
        filtersRow.add(fuelTypeCombo);
        filtersRow.add(new JLabel("Status"));
        filtersRow.add(statusCombo);
        filtersRow.add(new JLabel("Min Overall"));
        filtersRow.add(minOverallSpinner);
        filtersRow.add(new JLabel("Min clearance (mm)"));
        filtersRow.add(minGarageClearanceSpinner);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(this::clear);
        filtersRow.add(clearButton);

        add(filtersRow, BorderLayout.CENTER);

        ChangeListener changeListener = e -> notifyListeners();
        bodyTypeCombo.addActionListener(e -> notifyListeners());
        fuelTypeCombo.addActionListener(e -> notifyListeners());
        statusCombo.addActionListener(e -> notifyListeners());
        minOverallSpinner.addChangeListener(changeListener);
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
        snapMaxPriceSliderToIncrement();
        updatePriceValueLabel();
    }

    public VehicleFilterCriteria currentCriteria() {
        return new VehicleFilterCriteria(
                null,
                sliderMaxPrice(),
                selectedEnum(bodyTypeCombo, BodyType.class),
                selectedEnum(fuelTypeCombo, FuelType.class),
                spinnerScore(minOverallSpinner),
                spinnerDimension(minGarageClearanceSpinner),
                selectedEnum(statusCombo, VehicleStatus.class));
    }

    private void clear(ActionEvent event) {
        maxPriceSlider.setValue(maxPriceSlider.getMaximum());
        updatePriceValueLabel();
        bodyTypeCombo.setSelectedIndex(0);
        fuelTypeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        minOverallSpinner.setValue(0);
        minGarageClearanceSpinner.setValue(0);
        notifyListeners();
    }

    private void configurePriceSliderKeyboard() {
        maxPriceSlider.setFocusable(true);
        maxPriceSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maxPriceSlider.requestFocusInWindow();
            }
        });
        maxPriceSlider.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_UP -> {
                        stepMaxPriceSlider(-1);
                        e.consume();
                    }
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN -> {
                        stepMaxPriceSlider(1);
                        e.consume();
                    }
                    default -> {
                    }
                }
            }
        });
        maxPriceSlider.addPropertyChangeListener("UI", event -> bindPriceSliderKeyActions());

        bindPriceSliderKeyActions();
    }

    private void bindPriceSliderKeyActions() {
        InputMap inputMap = maxPriceSlider.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = maxPriceSlider.getActionMap();

        AbstractAction decrease = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepMaxPriceSlider(-1);
            }
        };
        AbstractAction increase = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepMaxPriceSlider(1);
            }
        };

        actionMap.put("priceDecrease", decrease);
        actionMap.put("priceIncrease", increase);
        actionMap.put("negativeIncrement", decrease);
        actionMap.put("positiveIncrement", increase);

        bindPriceStepKey(inputMap, KeyEvent.VK_LEFT, "priceDecrease");
        bindPriceStepKey(inputMap, KeyEvent.VK_UP, "priceDecrease");
        bindPriceStepKey(inputMap, KeyEvent.VK_RIGHT, "priceIncrease");
        bindPriceStepKey(inputMap, KeyEvent.VK_DOWN, "priceIncrease");
    }

    private static void bindPriceStepKey(InputMap inputMap, int keyCode, String action) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), action);
    }

    private void stepMaxPriceSlider(int direction) {
        int current = snappedPrice(maxPriceSlider.getValue());
        int next = current + direction * PRICE_SLIDER_INCREMENT_ZAR;
        next = Math.max(maxPriceSlider.getMinimum(), Math.min(maxPriceSlider.getMaximum(), next));
        maxPriceSlider.setValue(next);
    }

    private BigDecimal sliderMaxPrice() {
        return BigDecimal.valueOf(snappedPrice(maxPriceSlider.getValue()));
    }

    private int snappedPrice(int value) {
        int maximum = maxPriceSlider.getMaximum();
        int min = maxPriceSlider.getMinimum();
        if (value >= maximum) {
            return maximum;
        }
        int snapped = min + Math.round((value - min) / (float) PRICE_SLIDER_INCREMENT_ZAR) * PRICE_SLIDER_INCREMENT_ZAR;
        return Math.min(Math.max(min, snapped), maximum);
    }

    private void snapMaxPriceSliderToIncrement() {
        int snapped = snappedPrice(maxPriceSlider.getValue());
        if (snapped != maxPriceSlider.getValue()) {
            maxPriceSlider.setValue(snapped);
        }
    }

    private void updatePriceValueLabel() {
        priceValueLabel.setText("< R "
                + NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-ZA"))
                        .format(snappedPrice(maxPriceSlider.getValue())));
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
