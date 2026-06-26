package za.driver.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.persistence.ScoringProfileRepository;

public final class DefaultProfileSeeder {

    public static final UUID FAMILY_FOCUSED_ID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID BUDGET_FOCUSED_ID = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID EXECUTIVE_ID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    public static final UUID DAILY_COMMUTER_ID = UUID.fromString("6ba7b813-9dad-11d1-80b4-00c04fd430c8");

    private static final List<Supplier<ScoringProfile>> BUILT_IN_PROFILES = List.of(
            DefaultProfileSeeder::createFamilyFocusedProfile,
            DefaultProfileSeeder::createBudgetFocusedProfile,
            DefaultProfileSeeder::createExecutiveProfile,
            DefaultProfileSeeder::createDailyCommuterProfile);

    private DefaultProfileSeeder() {
    }

    public static void ensureDefaultProfiles(ScoringProfileRepository repository) throws IOException {
        for (Supplier<ScoringProfile> factory : BUILT_IN_PROFILES) {
            ScoringProfile profile = factory.get();
            if (repository.findById(profile.getId()).isEmpty()) {
                repository.save(profile);
            }
        }
    }

    static ScoringProfile createFamilyFocusedProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(FAMILY_FOCUSED_ID);
        profile.setName("Family Focused");
        configureProfile(
                profile,
                "Awesomeness",
                List.of(
                        weight(Metric.SAFETY, 25.0),
                        weight(Metric.RUNNING_COST, 15.0),
                        weight(Metric.RELIABILITY, 15.0),
                        weight(Metric.PERFORMANCE, 5.0),
                        weight(Metric.AWESOMENESS, 40.0)),
                List.of(
                        weight(Metric.PRESTIGE, 55.0),
                        weight(Metric.COMFORT, 15.0),
                        weight(Metric.DAILY_DRIVER, 15.0),
                        weight(Metric.TECHNOLOGY, 15.0)));
        return profile;
    }

    static ScoringProfile createBudgetFocusedProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(BUDGET_FOCUSED_ID);
        profile.setName("Budget Focused");
        configureProfile(
                profile,
                "Extras",
                List.of(
                        weight(Metric.RUNNING_COST, 35.0),
                        weight(Metric.RELIABILITY, 30.0),
                        weight(Metric.SAFETY, 20.0),
                        weight(Metric.DAILY_DRIVER, 5.0),
                        weight(Metric.AWESOMENESS, 10.0)),
                List.of(
                        weight(Metric.COMFORT, 30.0),
                        weight(Metric.TECHNOLOGY, 30.0),
                        weight(Metric.PERFORMANCE, 20.0),
                        weight(Metric.PRESTIGE, 20.0)));
        return profile;
    }

    static ScoringProfile createExecutiveProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(EXECUTIVE_ID);
        profile.setName("Executive");
        configureProfile(
                profile,
                "Ownership",
                List.of(
                        weight(Metric.PRESTIGE, 25.0),
                        weight(Metric.COMFORT, 25.0),
                        weight(Metric.PERFORMANCE, 20.0),
                        weight(Metric.TECHNOLOGY, 15.0),
                        weight(Metric.AWESOMENESS, 15.0)),
                List.of(
                        weight(Metric.SAFETY, 30.0),
                        weight(Metric.RELIABILITY, 30.0),
                        weight(Metric.RUNNING_COST, 20.0),
                        weight(Metric.DAILY_DRIVER, 20.0)));
        return profile;
    }

    static ScoringProfile createDailyCommuterProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(DAILY_COMMUTER_ID);
        profile.setName("Daily Commuter");
        configureProfile(
                profile,
                "Liveability",
                List.of(
                        weight(Metric.DAILY_DRIVER, 30.0),
                        weight(Metric.RUNNING_COST, 25.0),
                        weight(Metric.RELIABILITY, 25.0),
                        weight(Metric.SAFETY, 10.0),
                        weight(Metric.AWESOMENESS, 10.0)),
                List.of(
                        weight(Metric.COMFORT, 40.0),
                        weight(Metric.TECHNOLOGY, 30.0),
                        weight(Metric.PERFORMANCE, 15.0),
                        weight(Metric.PRESTIGE, 15.0)));
        return profile;
    }

    public static ScoringProfile newProfileTemplate() {
        ScoringProfile profile = new ScoringProfile();
        profile.setName("New Profile");
        applyFamilyFocusedDefaults(profile);
        return profile;
    }

    private static void applyFamilyFocusedDefaults(ScoringProfile profile) {
        configureProfile(
                profile,
                "Awesomeness",
                List.of(
                        weight(Metric.SAFETY, 25.0),
                        weight(Metric.RUNNING_COST, 15.0),
                        weight(Metric.RELIABILITY, 15.0),
                        weight(Metric.PERFORMANCE, 5.0),
                        weight(Metric.AWESOMENESS, 40.0)),
                List.of(
                        weight(Metric.PRESTIGE, 55.0),
                        weight(Metric.COMFORT, 15.0),
                        weight(Metric.DAILY_DRIVER, 15.0),
                        weight(Metric.TECHNOLOGY, 15.0)));
    }

    private static void configureProfile(
            ScoringProfile profile,
            String aggregateName,
            List<ScoringWeight> weights,
            List<ScoringWeight> aggregateComponents) {
        profile.setAggregateName(aggregateName);
        profile.setWeights(new ArrayList<>(weights));
        profile.setAggregateComponents(new ArrayList<>(aggregateComponents));
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
