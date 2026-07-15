package za.driver.presentation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import za.driver.model.BodyType;
import za.driver.model.DerivedMetrics;
import za.driver.model.Metric;
import za.driver.model.Pricing;
import za.driver.model.ScoringProfile;
import za.driver.model.Vehicle;
import za.driver.scoring.MetricScores;
import za.driver.scoring.TopWeightedMetrics;

public final class ModelGroup {

    static final int MAX_VISIBLE_TRIMS = 3;

    private final BodyType bodyType;
    private final String make;
    private final String model;
    private final String yearRange;
    private final String imageSlug;
    private final String imageFilename;
    private final Double averagedOverallScore;
    private final List<TrimEntry> trims;
    private final int hiddenTrimCount;
    private final String highlightsBlurb;

    public ModelGroup(
            BodyType bodyType,
            String make,
            String model,
            String yearRange,
            String imageSlug,
            String imageFilename,
            Double averagedOverallScore,
            List<TrimEntry> trims,
            int hiddenTrimCount,
            String highlightsBlurb) {
        this.bodyType = bodyType;
        this.make = make;
        this.model = model;
        this.yearRange = yearRange;
        this.imageSlug = imageSlug;
        this.imageFilename = imageFilename;
        this.averagedOverallScore = averagedOverallScore;
        this.trims = List.copyOf(trims);
        this.hiddenTrimCount = hiddenTrimCount;
        this.highlightsBlurb = highlightsBlurb == null ? "" : highlightsBlurb;
    }

    public BodyType bodyType() {
        return bodyType;
    }

    public String make() {
        return make;
    }

    public String model() {
        return model;
    }

    public String displayName() {
        StringBuilder name = new StringBuilder();
        if (make != null && !make.isBlank()) {
            name.append(make.trim());
        }
        if (model != null && !model.isBlank()) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(model.trim());
        }
        return name.toString();
    }

    public String yearRange() {
        return yearRange;
    }

    public String imageSlug() {
        return imageSlug;
    }

    public String imageFilename() {
        return imageFilename;
    }

    public Double averagedOverallScore() {
        return averagedOverallScore;
    }

    public List<TrimEntry> trims() {
        return trims;
    }

    public int hiddenTrimCount() {
        return hiddenTrimCount;
    }

    public String highlightsBlurb() {
        return highlightsBlurb;
    }

    public static List<BodyTypeSection> groupByBodyType(List<Vehicle> vehicles, ScoringProfile profile) {
        List<ModelGroup> groups = buildGroups(vehicles, profile);
        Map<BodyType, List<ModelGroup>> byBodyType = new LinkedHashMap<>();
        for (ModelGroup group : groups) {
            BodyType key = group.bodyType() == null ? BodyType.OTHER : group.bodyType();
            byBodyType.computeIfAbsent(key, ignored -> new ArrayList<>()).add(group);
        }

        List<BodyTypeSection> sections = new ArrayList<>();
        byBodyType.entrySet().stream()
                .sorted(Comparator.comparing(entry -> BodyTypeLabels.displayName(entry.getKey())))
                .forEach(entry -> {
                    List<ModelGroup> models = new ArrayList<>(entry.getValue());
                    models.sort(Comparator
                            .comparing(ModelGroup::averagedOverallScore, Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(ModelGroup::displayName, String.CASE_INSENSITIVE_ORDER));
                    sections.add(new BodyTypeSection(BodyTypeLabels.displayName(entry.getKey()), models));
                });
        return List.copyOf(sections);
    }

    static List<ModelGroup> flattenSections(List<BodyTypeSection> sections) {
        List<ModelGroup> groups = new ArrayList<>();
        for (BodyTypeSection section : sections) {
            groups.addAll(section.models());
        }
        return groups;
    }

    private static List<ModelGroup> buildGroups(List<Vehicle> vehicles, ScoringProfile profile) {
        Map<String, List<Vehicle>> grouped = new LinkedHashMap<>();
        for (Vehicle vehicle : vehicles) {
            String key = ImageSlug.groupKey(vehicle.getMake(), vehicle.getModel());
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(vehicle);
        }

        List<ModelGroup> groups = new ArrayList<>();
        for (List<Vehicle> trimsForModel : grouped.values()) {
            groups.add(buildGroup(trimsForModel, profile));
        }
        return groups;
    }

    private static ModelGroup buildGroup(List<Vehicle> vehicles, ScoringProfile profile) {
        Vehicle first = vehicles.getFirst();
        BodyType bodyType = first.getBodyType();
        String make = first.getMake();
        String model = first.getModel();
        String imageSlug = ImageSlug.of(make, model);
        String imageFilename = ImageSlug.filename(make, model);

        List<TrimEntry> allTrims = vehicles.stream()
                .map(vehicle -> toTrimEntry(vehicle, profile))
                .sorted(Comparator.comparing(TrimEntry::listPrice, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(TrimEntry::label, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int hiddenTrimCount = Math.max(0, allTrims.size() - MAX_VISIBLE_TRIMS);
        List<TrimEntry> visibleTrims = allTrims.stream().limit(MAX_VISIBLE_TRIMS).toList();

        return new ModelGroup(
                bodyType,
                make,
                model,
                formatYearRange(vehicles),
                imageSlug,
                imageFilename,
                averageOverallScore(vehicles),
                visibleTrims,
                hiddenTrimCount,
                buildHighlightsBlurb(vehicles));
    }

    private static TrimEntry toTrimEntry(Vehicle vehicle, ScoringProfile profile) {
        String derivative = vehicle.getDerivative();
        String label = derivative == null || derivative.isBlank() ? "Base" : derivative.trim();
        BigDecimal price = vehicle.getPricing() == null ? null : vehicle.getPricing().getListPrice();
        return new TrimEntry(label, price, buildRatingsForVehicle(vehicle, profile));
    }

    private static Double overallScore(Vehicle vehicle) {
        DerivedMetrics metrics = vehicle.getDerivedMetrics();
        if (metrics == null || metrics.getOverallScore() == null) {
            return null;
        }
        return metrics.getOverallScore();
    }

    private static Double averageOverallScore(List<Vehicle> vehicles) {
        double sum = 0.0;
        int count = 0;
        for (Vehicle vehicle : vehicles) {
            Double score = overallScore(vehicle);
            if (score != null) {
                sum += score;
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum / count;
    }

    private static String formatYearRange(List<Vehicle> vehicles) {
        Integer min = null;
        Integer max = null;
        for (Vehicle vehicle : vehicles) {
            Integer year = vehicle.getModelYear();
            if (year == null) {
                continue;
            }
            min = min == null ? year : Math.min(min, year);
            max = max == null ? year : Math.max(max, year);
        }
        if (min == null) {
            return "";
        }
        if (Objects.equals(min, max)) {
            return String.valueOf(min);
        }
        return min + "\u2013" + max;
    }

    private static List<RatingEntry> buildRatingsForVehicle(Vehicle vehicle, ScoringProfile profile) {
        List<RatingEntry> ratings = new ArrayList<>();
        Double overall = overallScore(vehicle);
        if (overall != null) {
            ratings.add(new RatingEntry("Overall", overall));
        }
        int weightedCount = profile != null && profile.getWeights() != null ? profile.getWeights().size() : 0;
        List<Metric> metrics = TopWeightedMetrics.topN(profile, Math.max(weightedCount, Metric.values().length));
        for (Metric metric : metrics) {
            Double score = MetricScores.displayScore(vehicle, vehicle.getDerivedMetrics(), metric);
            if (score == null) {
                continue;
            }
            ratings.add(new RatingEntry(
                    MetricLabels.displayName(metric),
                    score));
        }
        return ratings;
    }

    private static String buildHighlightsBlurb(List<Vehicle> vehicles) {
        for (Vehicle vehicle : vehicles) {
            String notes = vehicle.getNotes();
            if (notes != null && !notes.isBlank()) {
                return BlurbFormatter.truncateToWords(notes, BlurbFormatter.MAX_WORDS);
            }
        }
        return "";
    }
}
