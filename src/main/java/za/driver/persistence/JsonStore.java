package za.driver.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonStore {

    private final ObjectMapper mapper;

    public JsonStore() {
        this(createMapper());
    }

    public JsonStore(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static ObjectMapper createMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void ensureDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    public void write(Path file, Object value) throws IOException {
        ensureDirectory(file.getParent());
        mapper.writeValue(file.toFile(), value);
    }

    public <T> T read(Path file, Class<T> type) throws IOException {
        return mapper.readValue(file.toFile(), type);
    }
}
