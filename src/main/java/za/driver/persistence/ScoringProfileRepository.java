package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import za.driver.model.ScoringProfile;

public class ScoringProfileRepository extends JsonRepository<ScoringProfile> {

    private static final String SUBDIRECTORY = "profiles";

    public ScoringProfileRepository() {
        this(Paths.get("data"));
    }

    public ScoringProfileRepository(Path dataRoot) {
        super(dataRoot.resolve(SUBDIRECTORY), new JsonStore(), ScoringProfile.class);
    }

    ScoringProfileRepository(Path directory, JsonStore store) {
        super(directory, store, ScoringProfile.class);
    }

    @Override
    UUID extractId(ScoringProfile entity) {
        return entity.getId();
    }

    public void save(ScoringProfile profile) throws IOException {
        super.save(profile);
    }

    public Optional<ScoringProfile> findById(UUID id) throws IOException {
        return super.findById(id);
    }

    public List<ScoringProfile> findAll() throws IOException {
        return super.findAll();
    }

    public void delete(UUID id) throws IOException {
        super.delete(id);
    }
}
