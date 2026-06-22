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

        List<ScoringWeight> weights = new ArrayList<>();
        weights.add(weight(Metric.SAFETY, 25.0));
        weights.add(weight(Metric.RUNNING_COST, 15.0));
        weights.add(weight(Metric.RELIABILITY, 15.0));
        weights.add(weight(Metric.PERFORMANCE, 5.0));
        weights.add(weight(Metric.AWESOMENESS, 40.0));
        profile.setWeights(weights);
        return profile;
    }

    private static ScoringWeight weight(Metric metric, double value) {
        ScoringWeight scoringWeight = new ScoringWeight();
        scoringWeight.setMetric(metric);
        scoringWeight.setWeight(value);
        return scoringWeight;
    }
}
