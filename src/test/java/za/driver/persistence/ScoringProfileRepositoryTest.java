package za.driver.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.Metric;
import za.driver.model.ScoringProfile;

class ScoringProfileRepositoryTest {

    @TempDir
    Path tempDir;

    private ScoringProfileRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ScoringProfileRepository(tempDir);
    }

    @Test
    void saveAndLoad_profileWithWeights_roundTrips() throws IOException {
        ScoringProfile original = TestFixtures.scoringProfile();

        repository.save(original);
        ScoringProfile loaded = repository.findById(original.getId()).orElseThrow();

        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getName(), loaded.getName());
        assertEquals(5, loaded.getWeights().size());

        for (int i = 0; i < original.getWeights().size(); i++) {
            assertEquals(original.getWeights().get(i).getMetric(), loaded.getWeights().get(i).getMetric());
            assertEquals(original.getWeights().get(i).getWeight(), loaded.getWeights().get(i).getWeight());
        }

        assertEquals(Metric.SAFETY, loaded.getWeights().get(0).getMetric());
        assertEquals(25.0, loaded.getWeights().get(0).getWeight());
        assertEquals(40.0, loaded.getWeights().stream()
                .filter(w -> w.getMetric() == Metric.AWESOMENESS)
                .findFirst()
                .orElseThrow()
                .getWeight());
    }
}
