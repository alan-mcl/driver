package za.driver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.persistence.ScoringProfileRepository;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringService;

class DefaultProfileSeederTest {

    @TempDir
    Path tempDir;

    private ScoringProfileRepository profileRepository;
    private ScoringProfileService profileService;

    @BeforeEach
    void setUp() {
        profileRepository = new ScoringProfileRepository(tempDir);
        profileService = new ScoringProfileService(
                profileRepository,
                new VehicleRepository(tempDir),
                new ScoringService());
    }

    @Test
    void ensureDefaultProfiles_emptyRepo_seedsAllFourProfiles() throws IOException {
        DefaultProfileSeeder.ensureDefaultProfiles(profileRepository);

        List<ScoringProfile> profiles = profileRepository.findAll();
        assertEquals(4, profiles.size());
        assertProfilePresent(profiles, DefaultProfileSeeder.FAMILY_FOCUSED_ID, "Family Focused");
        assertProfilePresent(profiles, DefaultProfileSeeder.BUDGET_FOCUSED_ID, "Budget Focused");
        assertProfilePresent(profiles, DefaultProfileSeeder.EXECUTIVE_ID, "Executive");
        assertProfilePresent(profiles, DefaultProfileSeeder.DAILY_COMMUTER_ID, "Daily Commuter");

        for (ScoringProfile profile : profiles) {
            profileService.validateProfile(
                    profile.getName(),
                    profile.getWeights(),
                    profile.getAggregateName(),
                    profile.getAggregateComponents());
        }
    }

    @Test
    void ensureDefaultProfiles_familyAlreadyPresent_addsMissingThree() throws IOException {
        profileRepository.save(DefaultProfileSeeder.createFamilyFocusedProfile());
        Path familyFile = tempDir.resolve("profiles").resolve(DefaultProfileSeeder.FAMILY_FOCUSED_ID + ".json");
        String originalJson = java.nio.file.Files.readString(familyFile);

        DefaultProfileSeeder.ensureDefaultProfiles(profileRepository);

        List<ScoringProfile> profiles = profileRepository.findAll();
        assertEquals(4, profiles.size());
        assertEquals(originalJson, java.nio.file.Files.readString(familyFile));
    }

    @Test
    void ensureDefaultProfiles_secondRun_isIdempotent() throws IOException {
        DefaultProfileSeeder.ensureDefaultProfiles(profileRepository);
        DefaultProfileSeeder.ensureDefaultProfiles(profileRepository);

        assertEquals(4, profileRepository.findAll().size());
    }

    @Test
    void executiveProfile_partitionsTopAndAggregateMetrics() {
        ScoringProfile executive = DefaultProfileSeeder.createExecutiveProfile();

        Set<Metric> topMetrics = ScoringProfileService.extractTopMetrics(executive.getWeights());
        assertEquals(
                EnumSet.of(Metric.PRESTIGE, Metric.COMFORT, Metric.PERFORMANCE, Metric.TECHNOLOGY),
                topMetrics);

        Set<Metric> componentMetrics = EnumSet.noneOf(Metric.class);
        for (var component : executive.getAggregateComponents()) {
            componentMetrics.add(component.getMetric());
        }
        assertEquals(
                EnumSet.of(Metric.SAFETY, Metric.RELIABILITY, Metric.RUNNING_COST, Metric.DAILY_DRIVER),
                componentMetrics);
        assertEquals("Ownership", executive.getAggregateName());
    }

    private static void assertProfilePresent(List<ScoringProfile> profiles, java.util.UUID id, String name) {
        ScoringProfile profile = profiles.stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(name, profile.getName());
    }
}
