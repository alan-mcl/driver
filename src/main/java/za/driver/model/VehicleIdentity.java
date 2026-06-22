package za.driver.model;

public final class VehicleIdentity {

    private VehicleIdentity() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public static boolean matches(Vehicle left, Vehicle right) {
        if (left == null || right == null) {
            return false;
        }
        return matches(left.getMake(), left.getModel(), left.getDerivative(),
                right.getMake(), right.getModel(), right.getDerivative());
    }

    public static boolean matches(
            String make,
            String model,
            String derivative,
            String otherMake,
            String otherModel,
            String otherDerivative) {
        return normalize(make).equals(normalize(otherMake))
                && normalize(model).equals(normalize(otherModel))
                && normalize(derivative).equals(normalize(otherDerivative));
    }

    public static String label(Vehicle vehicle) {
        if (vehicle == null) {
            return "";
        }
        StringBuilder label = new StringBuilder();
        if (vehicle.getMake() != null && !vehicle.getMake().isBlank()) {
            label.append(vehicle.getMake().trim());
        }
        if (vehicle.getModel() != null && !vehicle.getModel().isBlank()) {
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(vehicle.getModel().trim());
        }
        if (vehicle.getDerivative() != null && !vehicle.getDerivative().isBlank()) {
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(vehicle.getDerivative().trim());
        }
        return label.toString();
    }

    public static String shortLabel(Vehicle vehicle) {
        if (vehicle == null) {
            return "";
        }
        StringBuilder label = new StringBuilder();
        if (vehicle.getModel() != null && !vehicle.getModel().isBlank()) {
            label.append(vehicle.getModel().trim());
        }
        if (vehicle.getDerivative() != null && !vehicle.getDerivative().isBlank()) {
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(vehicle.getDerivative().trim());
        }
        return label.toString();
    }
}
