package za.driver.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.model.TestDrive;

class TestDriveRepositoryTest {

    @TempDir
    Path tempDir;

    private TestDriveRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestDriveRepository(tempDir);
    }

    @Test
    void saveAndLoad_roundTrips() throws IOException {
        TestDrive original = TestFixtures.testDrive();

        repository.save(original);
        TestDrive loaded = repository.findById(original.getId()).orElseThrow();

        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getVehicleId(), loaded.getVehicleId());
        assertEquals(original.getDriveDate(), loaded.getDriveDate());
        assertEquals(original.getComfortRating(), loaded.getComfortRating());
        assertEquals(original.getVisibilityRating(), loaded.getVisibilityRating());
        assertEquals(original.getHandlingRating(), loaded.getHandlingRating());
        assertEquals(original.getSpouseApprovalRating(), loaded.getSpouseApprovalRating());
        assertEquals(original.getOverallImpression(), loaded.getOverallImpression());
        assertEquals(original.getNotes(), loaded.getNotes());
    }

    @Test
    void findByVehicleId_filtersCorrectly() throws IOException {
        repository.save(TestFixtures.testDrive());
        repository.save(TestFixtures.testDriveForOtherVehicle());

        assertEquals(1, repository.findByVehicleId(TestFixtures.VEHICLE_ID).size());
        assertEquals(TestFixtures.TEST_DRIVE_ID, repository.findByVehicleId(TestFixtures.VEHICLE_ID).get(0).getId());
    }
}
