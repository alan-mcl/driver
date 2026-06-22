package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BrandReliabilityLookupTest {

    private final BrandReliabilityLookup lookup = BrandReliabilityLookup.loadDefault();

    @Test
    void reliabilityScore_knownBrand_returnsScore() {
        assertEquals(95, lookup.reliabilityScore("Toyota"));
    }

    @Test
    void reliabilityScore_caseInsensitive() {
        assertEquals(95, lookup.reliabilityScore("toyota"));
    }

    @Test
    void reliabilityScore_unknownBrand_returnsNull() {
        assertNull(lookup.reliabilityScore("UnknownBrand"));
    }

    @Test
    void confidenceScore_knownBrand_returnsScore() {
        assertEquals(55, lookup.confidenceScore("Chery"));
    }

    @Test
    void confidenceScore_mercedesAlias_resolvesToMercedesBenz() {
        assertEquals(90, lookup.confidenceScore("Mercedes"));
        assertEquals("Mercedes-Benz", lookup.displayName("Mercedes"));
    }

    @Test
    void displayName_returnsCanonicalName() {
        assertEquals("Toyota", lookup.displayName("TOYOTA"));
    }
}
