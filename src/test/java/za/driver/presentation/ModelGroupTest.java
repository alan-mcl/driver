package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import za.driver.model.BodyType;
import za.driver.model.DerivedMetrics;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.model.VehicleStatus;
import za.driver.scoring.ScoringService;
import za.driver.scoring.ScoringTestFixtures;

class ModelGroupTest {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void groupByBodyType_groupsTrimsOfSameModel() {
        Vehicle base = trim("1.8 XS", new BigDecimal("350000"), 2024);
        Vehicle sport = trim("2.0 XR", new BigDecimal("420000"), 2024);
        sport.setId(UUID.randomUUID());

        List<BodyTypeSection> sections = ModelGroup.groupByBodyType(
                List.of(base, sport),
                ScoringTestFixtures.familyFocusedProfile());

        assertEquals(1, sections.size());
        assertEquals("Sedans", sections.getFirst().label());
        ModelGroup group = sections.getFirst().models().getFirst();
        assertEquals("Toyota", group.make());
        assertEquals("Corolla", group.model());
        assertEquals(BodyType.SEDAN, group.bodyType());
        assertEquals("2024", group.yearRange());
        assertEquals("toyota-corolla.jpg", group.imageFilename());
        assertEquals(2, group.trims().size());
        assertEquals("2.0 XR", group.trims().get(0).label());
        assertEquals(new BigDecimal("420000"), group.trims().get(0).listPrice());
        assertEquals("1.8 XS", group.trims().get(1).label());
        assertEquals(new BigDecimal("350000"), group.trims().get(1).listPrice());
        assertTrue(group.averagedOverallScore() != null && group.averagedOverallScore() > 0);
        assertEquals(6, group.trims().getFirst().ratings().size());
        assertEquals("Overall", group.trims().getFirst().ratings().getFirst().label());
        assertEquals("Awesomeness", group.trims().getFirst().ratings().get(1).label());
        assertEquals("Safety", group.trims().getFirst().ratings().get(2).label());
        assertEquals("Reliability", group.trims().getFirst().ratings().get(3).label());
        assertEquals("Running Cost", group.trims().getFirst().ratings().get(4).label());
        assertEquals("Performance", group.trims().getFirst().ratings().get(5).label());
        assertEquals(0, group.hiddenTrimCount());
    }

    @Test
    void groupByBodyType_separatesModelsByBodyType() {
        Vehicle sedan = trim("1.8 XS", new BigDecimal("350000"), 2024);
        Vehicle suv = trim("2.0 TSI", new BigDecimal("550000"), 2024);
        suv.setId(UUID.randomUUID());
        suv.setMake("Volkswagen");
        suv.setModel("Tiguan");
        suv.setBodyType(BodyType.SUV);

        List<BodyTypeSection> sections = ModelGroup.groupByBodyType(
                List.of(sedan, suv),
                ScoringTestFixtures.familyFocusedProfile());

        assertEquals(2, sections.size());
        assertEquals(List.of("SUVs", "Sedans"), sections.stream().map(BodyTypeSection::label).toList());
        assertEquals(1, sections.get(0).models().size());
        assertEquals(1, sections.get(1).models().size());
    }

    @Test
    void groupByBodyType_computesYearRangeAcrossTrims() {
        Vehicle older = trim("1.8 XS", new BigDecimal("350000"), 2023);
        Vehicle newer = trim("2.0 XR", new BigDecimal("420000"), 2024);
        newer.setId(UUID.randomUUID());

        ModelGroup group = ModelGroup.groupByBodyType(
                List.of(older, newer),
                ScoringTestFixtures.familyFocusedProfile()).getFirst().models().getFirst();

        assertEquals("2023\u20132024", group.yearRange());
    }

    @Test
    void groupByBodyType_allNullScores_yieldsNullAverage() {
        Vehicle vehicle = trim("1.8 XS", new BigDecimal("350000"), 2024);
        vehicle.setDerivedMetrics(new DerivedMetrics());

        ModelGroup group = ModelGroup.groupByBodyType(
                List.of(vehicle),
                ScoringTestFixtures.familyFocusedProfile()).getFirst().models().getFirst();

        assertNull(group.averagedOverallScore());
    }

    @Test
    void groupByBodyType_usesNotesAsHighlightsBlurbTruncatedToFiftyWords() {
        Vehicle vehicle = trim("1.8 XS", new BigDecimal("350000"), 2024);
        StringBuilder notes = new StringBuilder();
        for (int i = 1; i <= 60; i++) {
            notes.append("word").append(i).append(' ');
        }
        vehicle.setNotes(notes.toString().trim());

        ModelGroup group = ModelGroup.groupByBodyType(
                List.of(vehicle),
                ScoringTestFixtures.familyFocusedProfile()).getFirst().models().getFirst();

        assertTrue(group.highlightsBlurb().endsWith("\u2026"));
        assertEquals(50, group.highlightsBlurb().replace("\u2026", "").trim().split("\\s+").length);
    }

    @Test
    void groupByBodyType_limitsVisibleTrimsToThreeHighestPrice() {
        List<Vehicle> vehicles = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Vehicle vehicle = trim("Trim " + i, new BigDecimal(String.valueOf(300_000 + i * 10_000)), 2024);
            vehicle.setId(UUID.randomUUID());
            vehicles.add(vehicle);
        }

        ModelGroup group = ModelGroup.groupByBodyType(
                vehicles,
                ScoringTestFixtures.familyFocusedProfile()).getFirst().models().getFirst();

        assertEquals(3, group.trims().size());
        assertEquals(2, group.hiddenTrimCount());
        assertEquals("Trim 5", group.trims().getFirst().label());
        assertEquals(new BigDecimal("350000"), group.trims().getFirst().listPrice());
        assertEquals("Trim 3", group.trims().get(2).label());
    }

    @Test
    void groupByBodyType_populatesDealerOfferOnTrimEntry() {
        Vehicle vehicle = trim("1.8 XS", new BigDecimal("350000"), new BigDecimal("335000"), 2024);

        ModelGroup group = ModelGroup.groupByBodyType(
                List.of(vehicle),
                ScoringTestFixtures.familyFocusedProfile()).getFirst().models().getFirst();

        assertEquals(new BigDecimal("335000"), group.trims().getFirst().dealerOffer());
    }

    @Test
    void groupByPriceBand_assignsModelsByMinimumEffectivePrice() {
        Vehicle sedan = trim("1.8 XS", new BigDecimal("350000"), 2024);
        Vehicle suv = trim("2.0 TSI", new BigDecimal("550000"), 2024);
        suv.setId(UUID.randomUUID());
        suv.setMake("Volkswagen");
        suv.setModel("Tiguan");
        suv.setBodyType(BodyType.SUV);

        List<BodyTypeSection> sections = ModelGroup.groupByPriceBand(
                List.of(sedan, suv),
                ScoringTestFixtures.familyFocusedProfile(),
                CurrencyFormatter.defaults());

        assertEquals(2, sections.size());
        assertEquals("< R400k", sections.get(0).label());
        assertEquals(1, sections.get(0).models().size());
        assertEquals("Toyota Corolla", sections.get(0).models().getFirst().displayName());
        assertEquals("< R600k", sections.get(1).label());
        assertEquals("Volkswagen Tiguan", sections.get(1).models().getFirst().displayName());
    }

    @Test
    void groupByPriceBand_usesDealerOfferForEffectivePriceBand() {
        Vehicle expensiveTrim = trim("2.0 XR", new BigDecimal("420000"), 2024);
        Vehicle cheaperTrim = trim("1.8 XS", new BigDecimal("350000"), new BigDecimal("310000"), 2024);
        cheaperTrim.setId(UUID.randomUUID());

        List<BodyTypeSection> sections = ModelGroup.groupByPriceBand(
                List.of(expensiveTrim, cheaperTrim),
                ScoringTestFixtures.familyFocusedProfile(),
                CurrencyFormatter.defaults());

        assertEquals(1, sections.size());
        assertEquals("< R400k", sections.getFirst().label());
    }

    @Test
    void groupByPriceBand_putsUnpricedModelsInPriceTbcSectionLast() {
        Vehicle priced = trim("1.8 XS", new BigDecimal("350000"), 2024);
        Vehicle unpriced = trim("Base", null, 2024);
        unpriced.setId(UUID.randomUUID());
        unpriced.setMake("Honda");
        unpriced.setModel("Civic");

        List<BodyTypeSection> sections = ModelGroup.groupByPriceBand(
                List.of(priced, unpriced),
                ScoringTestFixtures.familyFocusedProfile(),
                CurrencyFormatter.defaults());

        assertEquals(2, sections.size());
        assertEquals("< R400k", sections.get(0).label());
        assertEquals(PriceBandLabels.UNPRICED_SECTION, sections.get(1).label());
        assertEquals("Honda Civic", sections.get(1).models().getFirst().displayName());
    }

    @Test
    void imageSlug_normalizesSpacesAndPunctuation() {
        assertEquals("toyota-corolla", ImageSlug.of("Toyota", "Corolla"));
        assertEquals("bmw-x3", ImageSlug.of("BMW", "X3"));
        assertEquals("mercedes-benz-c-class", ImageSlug.of("Mercedes-Benz", "C-Class"));
        assertEquals("toyota-corolla.jpg", ImageSlug.filename("Toyota", "Corolla"));
    }

    private Vehicle trim(String derivative, BigDecimal price, int year) {
        return trim(derivative, price, null, year);
    }

    private Vehicle trim(String derivative, BigDecimal price, BigDecimal dealerOffer, int year) {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.setDerivative(derivative);
        vehicle.setModelYear(year);
        vehicle.setStatus(VehicleStatus.SHORTLISTED);
        Pricing pricing = new Pricing();
        pricing.setListPrice(price);
        pricing.setDealerOffer(dealerOffer);
        vehicle.setPricing(pricing);
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, ScoringTestFixtures.familyFocusedProfile()));
        return vehicle;
    }
}
