package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import za.driver.model.BodyType;
import za.driver.model.Pricing;
import za.driver.model.Vehicle;
import za.driver.scoring.ScoringService;
import za.driver.scoring.ScoringTestFixtures;

class RevealPresentationBuilderTest {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void build_rendersBodyTypeSectionsWithRatingsAndHighlightsBlurb() {
        Vehicle base = vehicle("1.8 XS", new BigDecimal("350000"));
        base.setNotes("Spacious family sedan with strong safety scores and low running costs.");
        Vehicle sport = vehicle("2.0 XR", new BigDecimal("420000"));
        sport.setId(UUID.randomUUID());

        List<BodyTypeSection> sections = ModelGroup.groupByBodyType(
                List.of(base, sport),
                ScoringTestFixtures.familyFocusedProfile());
        String html = RevealPresentationBuilder.build(
                sections,
                ScoringTestFixtures.familyFocusedProfile(),
                LocalDateTime.of(2025, 6, 24, 10, 30),
                CurrencyFormatter.defaults());

        assertTrue(html.contains("class=\"body-type-slide\""));
        assertTrue(html.contains("Sedans"));
        assertTrue(html.contains("class=\"model-slide\""));
        assertFalse(html.contains("<section>\n        <section class=\"body-type-slide\">"));
        assertTrue(html.contains("images/toyota-corolla.jpg"));
        assertTrue(html.contains("1.8 XS"));
        assertTrue(html.contains("2.0 XR"));
        assertTrue(html.contains("R 350"));
        assertTrue(html.contains("R 420"));
        assertTrue(html.contains("trim-prices-overlay"));
        assertTrue(html.contains("trim-rating-block"));
        assertTrue(html.contains("Ratings"));
        assertTrue(html.contains("class=\"overall-rating\""));
        assertFalse(html.contains("/5"));
        assertTrue(html.contains("Overall"));
        assertTrue(html.contains("Safety"));
        assertTrue(html.contains("class=\"star filled\""));
        assertFalse(html.contains("class=\"rating\""));
        assertTrue(html.contains("Highlights"));
        assertTrue(html.contains("Spacious family sedan"));
        assertTrue(html.contains("reveal/dist/reveal.js"));
    }

    @Test
    void build_escapesHtmlInModelNamesAndBlurbs() {
        Vehicle vehicle = vehicle("1.8 &amp; XS", new BigDecimal("350000"));
        vehicle.setMake("Test<Make>");
        vehicle.setModel("Model\"One\"");
        vehicle.setNotes("Great value & low cost.");

        List<BodyTypeSection> sections = ModelGroup.groupByBodyType(
                List.of(vehicle),
                ScoringTestFixtures.familyFocusedProfile());
        String html = RevealPresentationBuilder.build(
                sections,
                ScoringTestFixtures.familyFocusedProfile(),
                LocalDateTime.of(2025, 6, 24, 10, 30),
                CurrencyFormatter.defaults());

        assertTrue(html.contains("Test&lt;Make&gt; Model&quot;One&quot;"));
        assertTrue(html.contains("Great value &amp; low cost."));
        assertFalse(html.contains("Test<Make>"));
    }

    @Test
    void build_groupsDifferentBodyTypesIntoSeparateSections() {
        Vehicle sedan = vehicle("1.8 XS", new BigDecimal("350000"));
        Vehicle suv = vehicle("2.0 TSI", new BigDecimal("550000"));
        suv.setId(UUID.randomUUID());
        suv.setMake("Volkswagen");
        suv.setModel("Tiguan");
        suv.setBodyType(BodyType.SUV);

        List<BodyTypeSection> sections = ModelGroup.groupByBodyType(
                List.of(sedan, suv),
                ScoringTestFixtures.familyFocusedProfile());
        String html = RevealPresentationBuilder.build(
                sections,
                ScoringTestFixtures.familyFocusedProfile(),
                LocalDateTime.of(2025, 6, 24, 10, 30),
                CurrencyFormatter.defaults());

        assertTrue(html.contains("Sedans"));
        assertTrue(html.contains("SUVs"));
    }

    private Vehicle vehicle(String derivative, BigDecimal price) {
        Vehicle vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.setDerivative(derivative);
        Pricing pricing = new Pricing();
        pricing.setListPrice(price);
        vehicle.setPricing(pricing);
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, ScoringTestFixtures.familyFocusedProfile()));
        return vehicle;
    }
}
