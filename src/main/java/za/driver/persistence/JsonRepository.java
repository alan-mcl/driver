package za.driver.persistence;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

abstract class JsonRepository<T> {

    protected final Path directory;
    protected final JsonStore store;
    protected final Class<T> type;

    JsonRepository(Path directory, JsonStore store, Class<T> type) {
        this.directory = directory;
        this.store = store;
        this.type = type;
        try {
            JsonStore.ensureDirectory(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory: " + directory, e);
        }
    }

    abstract UUID extractId(T entity);

    void save(T entity) throws IOException {
        UUID id = extractId(entity);
        if (id == null) {
            throw new IllegalArgumentException("Entity id must not be null");
        }
        store.write(directory.resolve(id + ".json"), entity);
    }

    Optional<T> findById(UUID id) throws IOException {
        Path file = directory.resolve(id + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        return Optional.of(store.read(file, type));
    }

    List<T> findAll() throws IOException {
        if (!Files.exists(directory)) {
            return List.of();
        }

        List<T> results = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : stream) {
                results.add(store.read(file, type));
            }
        }
        return results;
    }

    void delete(UUID id) throws IOException {
        Path file = directory.resolve(id + ".json");
        Files.deleteIfExists(file);
    }

    boolean exists(UUID id) {
        return Files.exists(directory.resolve(id + ".json"));
    }
}
