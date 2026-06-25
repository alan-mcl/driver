package za.driver.service;

import static za.driver.scoring.ScoringConstants.AWESOMENESS_COMFORT_WEIGHT;
import static za.driver.scoring.ScoringConstants.AWESOMENESS_DAILY_DRIVER_WEIGHT;
import static za.driver.scoring.ScoringConstants.AWESOMENESS_PRESTIGE_WEIGHT;
import static za.driver.scoring.ScoringConstants.AWESOMENESS_TECHNOLOGY_WEIGHT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.model.Vehicle;
import za.driver.persistence.ScoringProfileRepository;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringOverrides;
import za.driver.scoring.ScoringService;

public class ScoringProfileService {

    private static final Set<Metric> LEGACY_AWESOMENESS_COMPONENTS = EnumSet.of(
            Metric.COMFORT,
            Metric.DAILY_DRIVER,
            Metric.TECHNOLOGY,
            Metric.PRESTIGE);

    private final ScoringProfileRepository profileRepository;
    private final VehicleRepository vehicleRepository;
    private final ScoringService scoringService;

    public ScoringProfileService(
            ScoringProfileRepository profileRepository,
            VehicleRepository vehicleRepository,
            ScoringService scoringService) {
        this.profileRepository = profileRepository;
        this.vehicleRepository = vehicleRepository;
        this.scoringService = scoringService;
    }

    public void validateProfile(
            String name,
            List<ScoringWeight> weights,
            String aggregateName,
            List<ScoringWeight> aggregateComponents) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name is required");
        }
        if (aggregateName == null || aggregateName.isBlank()) {
            throw new IllegalArgumentException("Aggregate metric name is required");
        }
        validateWeights(weights);
        validateAggregateComponents(weights, aggregateComponents);
    }

    public void validateWeights(List<ScoringWeight> weights) {
        if (weights == null || weights.size() != 5) {
            throw new IllegalArgumentException("Weights must include exactly five profile-level metrics");
        }

        Set<Metric> seen = EnumSet.noneOf(Metric.class);
        double total = 0.0;
        boolean hasAggregate = false;
        int topCount = 0;

        for (ScoringWeight scoringWeight : weights) {
            if (scoringWeight.getMetric() == null) {
                throw new IllegalArgumentException("Each weight must specify a metric");
            }
            Metric metric = scoringWeight.getMetric();
            if (metric == Metric.AWESOMENESS) {
                hasAggregate = true;
            } else if (!Metric.BASE_METRICS.contains(metric)) {
                throw new IllegalArgumentException("Invalid profile metric: " + metric);
            } else {
                topCount++;
            }
            if (seen.contains(metric)) {
                throw new IllegalArgumentException("Duplicate metric: " + metric);
            }
            seen.add(metric);

            Double weight = scoringWeight.getWeight();
            if (weight == null || weight < 0.0) {
                throw new IllegalArgumentException("Each weight must be a non-negative number");
            }
            total += weight;
        }

        if (!hasAggregate || topCount != 4) {
            throw new IllegalArgumentException(
                    "Weights must include exactly one aggregate metric and four top metrics");
        }

        if (total != 100.0) {
            throw new IllegalArgumentException("Weights must total 100 (current total: " + total + ")");
        }
    }

    public void validateAggregateComponents(List<ScoringWeight> weights, List<ScoringWeight> aggregateComponents) {
        if (aggregateComponents == null || aggregateComponents.size() != 4) {
            throw new IllegalArgumentException("Aggregate composition must include exactly four component metrics");
        }

        Set<Metric> expectedComponents = complementTopMetrics(extractTopMetrics(weights));
        Set<Metric> seen = EnumSet.noneOf(Metric.class);
        double total = 0.0;

        for (ScoringWeight scoringWeight : aggregateComponents) {
            if (scoringWeight.getMetric() == null) {
                throw new IllegalArgumentException("Each aggregate component must specify a metric");
            }
            Metric metric = scoringWeight.getMetric();
            if (!Metric.BASE_METRICS.contains(metric)) {
                throw new IllegalArgumentException("Invalid aggregate component metric: " + metric);
            }
            if (seen.contains(metric)) {
                throw new IllegalArgumentException("Duplicate aggregate component metric: " + metric);
            }
            seen.add(metric);

            Double weight = scoringWeight.getWeight();
            if (weight == null || weight < 0.0) {
                throw new IllegalArgumentException("Each aggregate component weight must be a non-negative number");
            }
            total += weight;
        }

        if (!seen.equals(expectedComponents)) {
            throw new IllegalArgumentException(
                    "Aggregate components must be the four base metrics not chosen as top metrics");
        }

        if (total != 100.0) {
            throw new IllegalArgumentException(
                    "Aggregate component weights must total 100 (current total: " + total + ")");
        }
    }

    public List<ScoringWeight> migrateLegacyWeights(List<ScoringWeight> weights) {
        if (weights == null || weights.isEmpty()) {
            return weights;
        }

        boolean hasLegacy = weights.stream()
                .anyMatch(w -> w.getMetric() != null && LEGACY_AWESOMENESS_COMPONENTS.contains(w.getMetric()));
        if (!hasLegacy) {
            return weights;
        }

        double awesomenessWeight = 0.0;
        List<ScoringWeight> migrated = new ArrayList<>();

        for (ScoringWeight scoringWeight : weights) {
            Metric metric = scoringWeight.getMetric();
            if (metric == null || scoringWeight.getWeight() == null) {
                continue;
            }
            if (LEGACY_AWESOMENESS_COMPONENTS.contains(metric)) {
                awesomenessWeight += scoringWeight.getWeight();
            } else if (metric == Metric.AWESOMENESS) {
                awesomenessWeight += scoringWeight.getWeight();
            } else if (Metric.BASE_METRICS.contains(metric) || Metric.PROFILE_WEIGHT_METRICS.contains(metric)) {
                migrated.add(copyWeight(scoringWeight));
            }
        }

        ScoringWeight awesomeness = new ScoringWeight();
        awesomeness.setMetric(Metric.AWESOMENESS);
        awesomeness.setWeight(awesomenessWeight);
        migrated.add(awesomeness);
        return migrated;
    }

    public void updateWeights(ScoringProfile profile, List<ScoringWeight> weights) throws IOException {
        List<ScoringWeight> normalized = migrateLegacyWeights(weights);
        validateWeights(normalized);
        profile.setWeights(normalized);
        profileRepository.save(profile);
    }

    public void updateProfileAndRecalculateAll(
            ScoringProfile profile,
            String name,
            List<ScoringWeight> weights,
            String aggregateName,
            List<ScoringWeight> aggregateComponents) throws IOException {
        List<ScoringWeight> normalizedWeights = migrateLegacyWeights(weights);
        List<ScoringWeight> normalizedComponents = copyWeights(aggregateComponents);
        validateProfile(name, normalizedWeights, aggregateName, normalizedComponents);

        profile.setName(name.trim());
        profile.setWeights(normalizedWeights);
        profile.setAggregateName(aggregateName.trim());
        profile.setAggregateComponents(normalizedComponents);
        profileRepository.save(profile);
        recalculateAllVehicles(profile);
    }

    public void updateWeightsAndRecalculateAll(ScoringProfile profile, List<ScoringWeight> weights)
            throws IOException {
        String aggregateName = profile.getAggregateName() != null && !profile.getAggregateName().isBlank()
                ? profile.getAggregateName()
                : "Awesomeness";
        List<ScoringWeight> aggregateComponents = profile.getAggregateComponents();
        if (aggregateComponents == null || aggregateComponents.isEmpty()) {
            aggregateComponents = buildDefaultAggregateComponents(extractTopMetrics(migrateLegacyWeights(weights)));
        }
        updateProfileAndRecalculateAll(
                profile,
                profile.getName(),
                weights,
                aggregateName,
                aggregateComponents);
    }

    public void recalculateAllVehicles(ScoringProfile profile) throws IOException {
        for (Vehicle vehicle : vehicleRepository.findAll()) {
            ScoringOverrides overrides = ScoringOverrides.fromVehicle(vehicle);
            vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, overrides));
            vehicleRepository.save(vehicle);
        }
    }

    public ScoringProfile ensureMigratedProfile(ScoringProfile profile) throws IOException {
        boolean changed = false;

        List<ScoringWeight> migratedWeights = migrateLegacyWeights(profile.getWeights());
        if (migratedWeights != profile.getWeights()) {
            profile.setWeights(migratedWeights);
            changed = true;
        }

        if (profile.getAggregateName() == null || profile.getAggregateName().isBlank()) {
            profile.setAggregateName("Awesomeness");
            changed = true;
        }

        if (profile.getAggregateComponents() == null || profile.getAggregateComponents().isEmpty()) {
            profile.setAggregateComponents(buildDefaultAggregateComponents(extractTopMetrics(profile.getWeights())));
            changed = true;
        }

        if (changed) {
            profileRepository.save(profile);
        }
        return profile;
    }

    static Set<Metric> extractTopMetrics(List<ScoringWeight> weights) {
        Set<Metric> topMetrics = EnumSet.noneOf(Metric.class);
        if (weights == null) {
            return topMetrics;
        }
        for (ScoringWeight scoringWeight : weights) {
            Metric metric = scoringWeight.getMetric();
            if (metric != null && metric != Metric.AWESOMENESS) {
                topMetrics.add(metric);
            }
        }
        return topMetrics;
    }

    public static Set<Metric> complementTopMetrics(Set<Metric> topMetrics) {
        EnumSet<Metric> complement = EnumSet.copyOf(Metric.BASE_METRICS);
        complement.removeAll(topMetrics);
        return complement;
    }

    static List<ScoringWeight> buildDefaultAggregateComponents(Set<Metric> topMetrics) {
        List<ScoringWeight> components = new ArrayList<>();
        for (Metric metric : complementTopMetrics(topMetrics)) {
            components.add(weight(metric, defaultComponentWeight(metric)));
        }
        return components;
    }

    private static double defaultComponentWeight(Metric metric) {
        return switch (metric) {
            case PRESTIGE -> AWESOMENESS_PRESTIGE_WEIGHT;
            case COMFORT -> AWESOMENESS_COMFORT_WEIGHT;
            case DAILY_DRIVER -> AWESOMENESS_DAILY_DRIVER_WEIGHT;
            case TECHNOLOGY -> AWESOMENESS_TECHNOLOGY_WEIGHT;
            default -> throw new IllegalArgumentException("Not an aggregate component metric: " + metric);
        };
    }

    private static ScoringWeight copyWeight(ScoringWeight source) {
        ScoringWeight copy = new ScoringWeight();
        copy.setMetric(source.getMetric());
        copy.setWeight(source.getWeight());
        return copy;
    }

    private static List<ScoringWeight> copyWeights(List<ScoringWeight> source) {
        List<ScoringWeight> copies = new ArrayList<>();
        if (source == null) {
            return copies;
        }
        for (ScoringWeight scoringWeight : source) {
            copies.add(copyWeight(scoringWeight));
        }
        return copies;
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
