package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import za.driver.model.TestDrive;

public class TestDriveRepository extends JsonRepository<TestDrive> {

    private static final String SUBDIRECTORY = "test-drives";

    public TestDriveRepository() {
        this(Paths.get("data"));
    }

    public TestDriveRepository(Path dataRoot) {
        super(dataRoot.resolve(SUBDIRECTORY), new JsonStore(), TestDrive.class);
    }

    TestDriveRepository(Path directory, JsonStore store) {
        super(directory, store, TestDrive.class);
    }

    @Override
    UUID extractId(TestDrive entity) {
        return entity.getId();
    }

    public List<TestDrive> findByVehicleId(UUID vehicleId) throws IOException {
        return findAll().stream()
                .filter(drive -> vehicleId.equals(drive.getVehicleId()))
                .toList();
    }
}
