package za.driver.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;
import za.driver.model.ScoringWeight;
import za.driver.persistence.ScoringProfileRepository;

public final class DefaultProfileSeeder {

    private static final UUID FAMILY_FOCUSED_ID = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    private DefaultProfileSeeder() {
    }

    public static ScoringProfile ensureDefaultProfile(ScoringProfileRepository repository) throws IOException {
        List<ScoringProfile> profiles = repository.findAll();
        if (profiles.isEmpty()) {
            repository.save(createFamilyFocusedProfile());
            profiles = repository.findAll();
        }
        return profiles.get(0);
    }

    static ScoringProfile createFamilyFocusedProfile() {
        ScoringProfile profile = new ScoringProfile();
        profile.setId(FAMILY_FOCUSED_ID);
        profile.setName("Family Focused");
        applyFamilyFocusedDefaults(profile);
        return profile;
    }

    public static ScoringProfile newProfileTemplate() {
        ScoringProfile profile = new ScoringProfile();
        profile.setName("New Profile");
        applyFamilyFocusedDefaults(profile);
        return profile;
    }

    private static void applyFamilyFocusedDefaults(ScoringProfile profile) {
        profile.setAggregateName("Awesomeness");

        List<ScoringWeight> weights = new ArrayList<>();
        weights.add(weight(Metric.SAFETY, 25.0));
        weights.add(weight(Metric.RUNNING_COST, 15.0));
        weights.add(weight(Metric.RELIABILITY, 15.0));
        weights.add(weight(Metric.PERFORMANCE, 5.0));
        weights.add(weight(Metric.AWESOMENESS, 40.0));
        profile.setWeights(weights);

        List<ScoringWeight> aggregateComponents = new ArrayList<>();
        aggregateComponents.add(weight(Metric.PRESTIGE, 55.0));
        aggregateComponents.add(weight(Metric.COMFORT, 15.0));
        aggregateComponents.add(weight(Metric.DAILY_DRIVER, 15.0));
        aggregateComponents.add(weight(Metric.TECHNOLOGY, 15.0));
        profile.setAggregateComponents(aggregateComponents);
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
