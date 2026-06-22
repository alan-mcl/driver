package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import za.driver.model.Vehicle;

public class VehicleRepository extends JsonRepository<Vehicle> {

    private static final String SUBDIRECTORY = "vehicles";

    public VehicleRepository() {
        this(Paths.get("data"));
    }

    public VehicleRepository(Path dataRoot) {
        super(dataRoot.resolve(SUBDIRECTORY), new JsonStore(), Vehicle.class);
    }

    VehicleRepository(Path directory, JsonStore store) {
        super(directory, store, Vehicle.class);
    }

    @Override
    UUID extractId(Vehicle entity) {
        return entity.getId();
    }

    public void save(Vehicle vehicle) throws IOException {
        super.save(vehicle);
    }

    public Optional<Vehicle> findById(UUID id) throws IOException {
        return super.findById(id);
    }

    public List<Vehicle> findAll() throws IOException {
        return super.findAll();
    }

    public void delete(UUID id) throws IOException {
        super.delete(id);
    }
}
