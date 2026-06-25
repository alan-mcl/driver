package za.driver.presentation;

import za.driver.model.BodyType;

public final class BodyTypeLabels {

    private BodyTypeLabels() {
    }

    public static String displayName(BodyType bodyType) {
        if (bodyType == null) {
            return "Other";
        }
        return switch (bodyType) {
            case HATCHBACK -> "Hatchbacks";
            case SEDAN -> "Sedans";
            case WAGON -> "Wagons";
            case CROSSOVER -> "Crossovers";
            case SUV -> "SUVs";
            case MPV -> "MPVs";
            case COUPE -> "Coupes";
            case CONVERTIBLE -> "Convertibles";
            case BAKKIE -> "Bakkies";
            case OTHER -> "Other";
        };
    }
}
