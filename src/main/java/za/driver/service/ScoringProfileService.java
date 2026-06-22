package za.driver.service;

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

    public void validateWeights(List<ScoringWeight> weights) {
        if (weights == null || weights.size() != Metric.PROFILE_WEIGHT_METRICS.size()) {
            throw new IllegalArgumentException("Weights must include all five profile metrics");
        }

        Set<Metric> seen = EnumSet.noneOf(Metric.class);
        double total = 0.0;

        for (ScoringWeight scoringWeight : weights) {
            if (scoringWeight.getMetric() == null) {
                throw new IllegalArgumentException("Each weight must specify a metric");
            }
            if (!Metric.PROFILE_WEIGHT_METRICS.contains(scoringWeight.getMetric())) {
                throw new IllegalArgumentException("Invalid profile metric: " + scoringWeight.getMetric());
            }
            if (seen.contains(scoringWeight.getMetric())) {
                throw new IllegalArgumentException("Duplicate metric: " + scoringWeight.getMetric());
            }
            seen.add(scoringWeight.getMetric());

            Double weight = scoringWeight.getWeight();
            if (weight == null || weight < 0.0) {
                throw new IllegalArgumentException("Each weight must be a non-negative number");
            }
            total += weight;
        }

        if (total != 100.0) {
            throw new IllegalArgumentException("Weights must total 100 (current total: " + total + ")");
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
            } else if (Metric.PROFILE_WEIGHT_METRICS.contains(metric)) {
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

    public void recalculateAllVehicles(ScoringProfile profile) throws IOException {
        for (Vehicle vehicle : vehicleRepository.findAll()) {
            ScoringOverrides overrides = ScoringOverrides.fromVehicle(vehicle);
            vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, overrides));
            vehicleRepository.save(vehicle);
        }
    }

    public void updateWeightsAndRecalculateAll(ScoringProfile profile, List<ScoringWeight> weights)
            throws IOException {
        updateWeights(profile, weights);
        recalculateAllVehicles(profile);
    }

    public ScoringProfile ensureMigratedProfile(ScoringProfile profile) throws IOException {
        List<ScoringWeight> migrated = migrateLegacyWeights(profile.getWeights());
        if (migrated == profile.getWeights()) {
            return profile;
        }
        profile.setWeights(migrated);
        profileRepository.save(profile);
        return profile;
    }

    private static ScoringWeight copyWeight(ScoringWeight source) {
        ScoringWeight copy = new ScoringWeight();
        copy.setMetric(source.getMetric());
        copy.setWeight(source.getWeight());
        return copy;
    }
}
