package za.driver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.DerivedMetrics;
import za.driver.model.ManualScoreOverrides;
import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.model.Vehicle;
import za.driver.persistence.ScoringProfileRepository;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringOverrides;
import za.driver.scoring.ScoringService;
import za.driver.scoring.ScoringTestFixtures;

class ScoringProfileServiceTest {

    @TempDir
    Path tempDir;

    private ScoringProfileRepository profileRepository;
    private VehicleRepository vehicleRepository;
    private ScoringProfileService profileService;
    private ScoringProfile profile;

    @BeforeEach
    void setUp() {
        profileRepository = new ScoringProfileRepository(tempDir);
        vehicleRepository = new VehicleRepository(tempDir);
        ScoringService scoringService = new ScoringService();
        profileService = new ScoringProfileService(profileRepository, vehicleRepository, scoringService);
        profile = ScoringTestFixtures.familyFocusedProfile();
    }

    @Test
    void validateWeights_rejectsWhenSumNot100() {
        List<ScoringWeight> weights = copyWeights(profile.getWeights());
        weights.get(0).setWeight(30.0);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.validateWeights(weights));
        assertTrue(error.getMessage().contains("100"));
    }

    @Test
    void validateWeights_rejectsMissingMetric() {
        List<ScoringWeight> weights = new ArrayList<>(profile.getWeights());
        weights.remove(weights.size() - 1);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.validateWeights(weights));
        assertTrue(error.getMessage().contains("five profile-level metrics"));
    }

    @Test
    void migrateLegacyWeights_sumsComponentWeightsIntoAwesomeness() {
        List<ScoringWeight> legacy = legacyWeights();

        List<ScoringWeight> migrated = profileService.migrateLegacyWeights(legacy);

        assertEquals(5, migrated.size());
        assertEquals(40.0, weightFor(migrated, Metric.AWESOMENESS));
        assertEquals(25.0, weightFor(migrated, Metric.SAFETY));
        assertFalse(migrated.stream().anyMatch(w -> w.getMetric() == Metric.COMFORT));
        assertFalse(migrated.stream().anyMatch(w -> w.getMetric() == Metric.PRESTIGE));
    }

    @Test
    void updateWeightsAndRecalculateAll_persistsProfileAndUpdatesOverallScores() throws IOException {
        profileRepository.save(profile);

        Vehicle full = ScoringTestFixtures.fullVehicle();
        ScoringService scoringService = new ScoringService();
        full.setDerivedMetrics(scoringService.calculate(full, profile));
        vehicleRepository.save(full);

        Double originalOverall = full.getDerivedMetrics().getOverallScore();
        Double originalSafety = full.getDerivedMetrics().getSafetyScore();
        assertNotNull(originalOverall);
        assertNotNull(originalSafety);

        List<ScoringWeight> safetyHeavyWeights = safetyHeavyWeights();
        profileService.updateWeightsAndRecalculateAll(profile, safetyHeavyWeights);

        ScoringProfile loadedProfile = profileRepository.findById(profile.getId()).orElseThrow();
        assertEquals(50.0, loadedProfile.getWeights().stream()
                .filter(w -> w.getMetric() == Metric.SAFETY)
                .findFirst()
                .orElseThrow()
                .getWeight());

        Vehicle reloadedFull = vehicleRepository.findById(full.getId()).orElseThrow();
        assertEquals(originalSafety, reloadedFull.getDerivedMetrics().getSafetyScore());
        assertNotNull(reloadedFull.getDerivedMetrics().getOverallScore());
        assertTrue(reloadedFull.getDerivedMetrics().getOverallScore() > originalOverall);
    }

    @Test
    void recalculateAllVehicles_preservesReliabilityOverrideInPersistedMetrics() throws IOException {
        profileRepository.save(profile);

        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        ManualScoreOverrides manualOverrides = new ManualScoreOverrides();
        manualOverrides.setReliabilityManualEstimate(90.0);
        vehicle.setManualScoreOverrides(manualOverrides);
        ScoringService scoringService = new ScoringService();
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, ScoringOverrides.fromVehicle(vehicle)));
        vehicleRepository.save(vehicle);

        profileService.recalculateAllVehicles(profile);

        Vehicle reloaded = vehicleRepository.findById(vehicle.getId()).orElseThrow();
        DerivedMetrics metrics = reloaded.getDerivedMetrics();
        assertNotNull(metrics);
        assertEquals(91.0, metrics.getReliabilityHeuristic());
        assertEquals(90.0, reloaded.getManualScoreOverrides().getReliabilityManualEstimate());
        assertEquals(91.0, metrics.getReliabilityScore());
    }

    @Test
    void recalculateAllVehicles_blendsManualEstimateWithHeuristic() throws IOException {
        profileRepository.save(profile);

        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        ManualScoreOverrides manualOverrides = new ManualScoreOverrides();
        manualOverrides.setReliabilityManualEstimate(88.0);
        vehicle.setManualScoreOverrides(manualOverrides);
        ScoringService scoringService = new ScoringService();
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile, ScoringOverrides.fromVehicle(vehicle)));
        vehicleRepository.save(vehicle);

        profileService.recalculateAllVehicles(profile);

        Vehicle reloaded = vehicleRepository.findById(vehicle.getId()).orElseThrow();
        assertEquals(90.0, reloaded.getDerivedMetrics().getReliabilityScore());
    }

    @Test
    void validateAggregateComponents_rejectsPartitionMismatch() {
        List<ScoringWeight> weights = copyWeights(profile.getWeights());
        List<ScoringWeight> components = copyWeights(profile.getAggregateComponents());
        components.get(0).setMetric(Metric.SAFETY);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> profileService.validateAggregateComponents(weights, components));
        assertTrue(error.getMessage().contains("not chosen as top metrics"));
    }

    @Test
    void ensureMigratedProfile_backfillsAggregateFields() throws IOException {
        ScoringProfile legacy = new ScoringProfile();
        legacy.setId(UUID.randomUUID());
        legacy.setName("Legacy");
        legacy.setWeights(copyWeights(profile.getWeights()));

        ScoringProfile migrated = profileService.ensureMigratedProfile(legacy);

        assertEquals("Awesomeness", migrated.getAggregateName());
        assertEquals(4, migrated.getAggregateComponents().size());
        assertEquals(55.0, weightFor(migrated.getAggregateComponents(), Metric.PRESTIGE));
    }

    @Test
    void updateProfileAndRecalculateAll_persistsAggregateNameAndComponents() throws IOException {
        profileRepository.save(profile);

        Vehicle full = ScoringTestFixtures.fullVehicle();
        ScoringService scoringService = new ScoringService();
        full.setDerivedMetrics(scoringService.calculate(full, profile));
        vehicleRepository.save(full);

        List<ScoringWeight> weights = copyWeights(profile.getWeights());
        List<ScoringWeight> components = copyWeights(profile.getAggregateComponents());
        components.stream()
                .filter(w -> w.getMetric() == Metric.COMFORT)
                .findFirst()
                .orElseThrow()
                .setWeight(30.0);
        components.stream()
                .filter(w -> w.getMetric() == Metric.PRESTIGE)
                .findFirst()
                .orElseThrow()
                .setWeight(40.0);

        profileService.updateProfileAndRecalculateAll(
                profile,
                "Renamed Profile",
                weights,
                "Cool Factor",
                components);

        ScoringProfile loaded = profileRepository.findById(profile.getId()).orElseThrow();
        assertEquals("Renamed Profile", loaded.getName());
        assertEquals("Cool Factor", loaded.getAggregateName());
        assertEquals(30.0, weightFor(loaded.getAggregateComponents(), Metric.COMFORT));

        Vehicle reloaded = vehicleRepository.findById(full.getId()).orElseThrow();
        assertNotNull(reloaded.getDerivedMetrics().getAwesomenessScore());
    }

    @Test
    void recalculateAllVehicles_withoutOverride_recomputesReliability() throws IOException {
        profileRepository.save(profile);

        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        ScoringService scoringService = new ScoringService();
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, profile));
        vehicleRepository.save(vehicle);

        profileService.recalculateAllVehicles(profile);

        Vehicle reloaded = vehicleRepository.findById(vehicle.getId()).orElseThrow();
        assertEquals(91.0, reloaded.getDerivedMetrics().getReliabilityScore());
        assertEquals(91.0, reloaded.getDerivedMetrics().getReliabilityHeuristic());
        assertNull(reloaded.getManualScoreOverrides());
    }

    private static List<ScoringWeight> copyWeights(List<ScoringWeight> source) {
        List<ScoringWeight> weights = new ArrayList<>();
        for (ScoringWeight sourceWeight : source) {
            ScoringWeight copy = new ScoringWeight();
            copy.setMetric(sourceWeight.getMetric());
            copy.setWeight(sourceWeight.getWeight());
            weights.add(copy);
        }
        return weights;
    }

    private static List<ScoringWeight> legacyWeights() {
        List<ScoringWeight> weights = new ArrayList<>();
        weights.add(weight(Metric.SAFETY, 25.0));
        weights.add(weight(Metric.RUNNING_COST, 15.0));
        weights.add(weight(Metric.RELIABILITY, 15.0));
        weights.add(weight(Metric.COMFORT, 10.0));
        weights.add(weight(Metric.PERFORMANCE, 5.0));
        weights.add(weight(Metric.DAILY_DRIVER, 15.0));
        weights.add(weight(Metric.TECHNOLOGY, 5.0));
        weights.add(weight(Metric.PRESTIGE, 10.0));
        return weights;
    }

    private static List<ScoringWeight> safetyHeavyWeights() {
        List<ScoringWeight> weights = new ArrayList<>();
        weights.add(weight(Metric.SAFETY, 50.0));
        weights.add(weight(Metric.RUNNING_COST, 10.0));
        weights.add(weight(Metric.RELIABILITY, 10.0));
        weights.add(weight(Metric.PERFORMANCE, 5.0));
        weights.add(weight(Metric.AWESOMENESS, 25.0));
        return weights;
    }

    private static double weightFor(List<ScoringWeight> weights, Metric metric) {
        return weights.stream()
                .filter(w -> w.getMetric() == metric)
                .findFirst()
                .orElseThrow()
                .getWeight();
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
