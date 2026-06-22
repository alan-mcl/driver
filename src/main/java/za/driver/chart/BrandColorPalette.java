package za.driver.chart;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import za.driver.model.VehicleIdentity;

public final class BrandColorPalette {

    private static final Color[] COLORS = {
            new Color(31, 119, 180),
            new Color(255, 127, 14),
            new Color(44, 160, 44),
            new Color(214, 39, 40),
            new Color(148, 103, 189),
            new Color(140, 86, 75),
            new Color(227, 119, 194),
            new Color(127, 127, 127),
            new Color(188, 189, 34),
            new Color(23, 190, 207),
            new Color(174, 199, 232),
            new Color(255, 187, 120),
    };

    private final Map<String, Color> makeColors = new LinkedHashMap<>();
    private final Map<String, String> makeDisplayNames = new LinkedHashMap<>();

    public Color colorForMake(String make) {
        String key = normalizeMake(make);
        registerDisplayName(key, make);
        return makeColors.computeIfAbsent(key, this::assignColor);
    }

    public Map<String, Color> assignedColors() {
        return Map.copyOf(makeColors);
    }

    public Map<String, String> displayNames() {
        return Map.copyOf(makeDisplayNames);
    }

    public void registerMake(String make) {
        colorForMake(make);
    }

    private void registerDisplayName(String key, String make) {
        if (!makeDisplayNames.containsKey(key)) {
            if (make == null || make.isBlank()) {
                makeDisplayNames.put(key, "(unknown)");
            } else {
                makeDisplayNames.put(key, make.trim());
            }
        }
    }

    private Color assignColor(String normalizedMake) {
        int index = Math.floorMod(normalizedMake.hashCode(), COLORS.length);
        return COLORS[index];
    }

    private static String normalizeMake(String make) {
        if (make == null || make.isBlank()) {
            return "(unknown)";
        }
        return VehicleIdentity.normalize(make);
    }
}
