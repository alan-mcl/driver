package za.driver.import_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.driver.model.DataQuality;
import za.driver.model.VehicleStatus;

class ImportServiceTest {

    private ImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ImportService();
    }

    @Test
    void parse_validJson_returnsValidResult() {
        ImportResult result = importService.parse(minimalJson());

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertEquals("Toyota", result.getVehicle().getMake());
        assertEquals("Corolla", result.getVehicle().getModel());
        assertEquals(VehicleStatus.CANDIDATE, result.getVehicle().getStatus());
    }

    @Test
    void parse_partialJson_succeeds() {
        ImportResult result = importService.parse(minimalJson());

        assertTrue(result.isValid());
        assertNull(result.getVehicle().getEngine());
        assertNull(result.getVehicle().getPricing());
        assertNull(result.getVehicle().getSafety());
    }

    @Test
    void parse_invalidEnum_returnsFailure() {
        String json = """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "INVALID"
                  }
                }
                """;

        ImportResult result = importService.parse(json);

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void parse_malformedJson_returnsFailure() {
        ImportResult result = importService.parse("{ not json");

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).startsWith("Invalid JSON:"));
    }

    @Test
    void preview_preservesDataQuality() {
        String json = """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "CANDIDATE"
                  },
                  "dataQuality": {
                    "pricing.price": "VERIFIED"
                  }
                }
                """;

        ImportResult result = importService.preview(json);

        assertTrue(result.isValid());
        assertEquals(DataQuality.VERIFIED, result.getDataQuality().get("pricing.price"));
    }

    @Test
    void parse_missingMake_returnsValidationFailure() {
        String json = """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "model": "Corolla",
                    "status": "CANDIDATE"
                  }
                }
                """;

        ImportResult result = importService.parse(json);

        assertFalse(result.isValid());
        assertNotNull(result.getVehicle());
        assertTrue(result.getErrors().get(0).toLowerCase().contains("make"));
    }

    @Test
    void parse_multipleVehicles_returnsAll() {
        ImportResult result = importService.parse(batchJson());

        assertTrue(result.isValid());
        assertEquals(2, result.getVehicleCount());
        assertEquals("Toyota", result.getEntries().get(0).getVehicle().getMake());
        assertEquals("Honda", result.getEntries().get(1).getVehicle().getMake());
    }

    @Test
    void parse_duplicateIdentityInBatch_returnsFailure() {
        String json = """
                {
                  "schemaVersion": 1,
                  "vehicles": [
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "make": "Toyota",
                      "model": "Corolla",
                      "derivative": "1.8 XS",
                      "status": "CANDIDATE"
                    },
                    {
                      "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                      "make": "Toyota",
                      "model": "Corolla",
                      "derivative": "1.8 XS",
                      "status": "CANDIDATE"
                    }
                  ]
                }
                """;

        ImportResult result = importService.parse(json);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.toLowerCase().contains("duplicate")));
    }

    @Test
    void parse_noVehicles_returnsFailure() {
        String json = """
                {
                  "schemaVersion": 1
                }
                """;

        ImportResult result = importService.parse(json);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.toLowerCase().contains("vehicle")));
    }

    private static String batchJson() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicles": [
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "make": "Toyota",
                      "model": "Corolla",
                      "status": "CANDIDATE"
                    },
                    {
                      "vehicle": {
                        "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                        "make": "Honda",
                        "model": "Civic",
                        "status": "CANDIDATE"
                      },
                      "dataQuality": {
                        "pricing.price": "VERIFIED"
                      }
                    }
                  ]
                }
                """;
    }

    private static String minimalJson() {
        return """
                {
                  "schemaVersion": 1,
                  "vehicle": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "make": "Toyota",
                    "model": "Corolla",
                    "status": "CANDIDATE"
                  }
                }
                """;
    }
}
