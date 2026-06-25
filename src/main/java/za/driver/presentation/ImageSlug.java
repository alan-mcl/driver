package za.driver.presentation;

import za.driver.model.VehicleIdentity;

public final class ImageSlug {

    private ImageSlug() {
    }

    public static String of(String make, String model) {
        String combined = (safe(make) + " " + safe(model)).trim();
        if (combined.isEmpty()) {
            return "vehicle";
        }
        String slug = combined.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-");
        return slug.isEmpty() ? "vehicle" : slug;
    }

    public static String filename(String make, String model) {
        return of(make, model) + ".jpg";
    }

    public static String groupKey(String make, String model) {
        return VehicleIdentity.normalize(make) + "|" + VehicleIdentity.normalize(model);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
