package za.driver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PricingTest {

    @Test
    void effectivePrice_prefersDealerOfferWhenSet() {
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("350000"));
        pricing.setDealerOffer(new BigDecimal("320000"));

        assertEquals(new BigDecimal("320000"), pricing.effectivePrice());
    }

    @Test
    void effectivePrice_fallsBackToListPrice() {
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("350000"));

        assertEquals(new BigDecimal("350000"), pricing.effectivePrice());
    }

    @Test
    void effectivePrice_ignoresNonPositiveDealerOffer() {
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("350000"));
        pricing.setDealerOffer(BigDecimal.ZERO);

        assertEquals(new BigDecimal("350000"), pricing.effectivePrice());
    }

    @Test
    void effectivePrice_returnsNullWhenNoPrices() {
        Pricing pricing = new Pricing();

        assertNull(pricing.effectivePrice());
    }

    @Test
    void filterPrice_returnsLowestWhenBothSet() {
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("450000"));
        pricing.setDealerOffer(new BigDecimal("380000"));

        assertEquals(new BigDecimal("380000"), pricing.filterPrice());
    }

    @Test
    void filterPrice_fallsBackToSinglePrice() {
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("450000"));

        assertEquals(new BigDecimal("450000"), pricing.filterPrice());
    }

    @Test
    void normalizeDates_defaultsDealerDateFromListDate() {
        Pricing pricing = new Pricing();
        pricing.setListPrice(new BigDecimal("350000"));
        pricing.setDealerOffer(new BigDecimal("320000"));
        pricing.setListPriceDate(LocalDate.of(2026, 6, 17));

        pricing.normalizeDates();

        assertEquals(LocalDate.of(2026, 6, 17), pricing.getDealerOfferDate());
    }

    @Test
    void normalizeDates_preservesExplicitDealerDate() {
        Pricing pricing = new Pricing();
        pricing.setDealerOffer(new BigDecimal("320000"));
        pricing.setListPriceDate(LocalDate.of(2026, 6, 17));
        pricing.setDealerOfferDate(LocalDate.of(2026, 6, 20));

        pricing.normalizeDates();

        assertEquals(LocalDate.of(2026, 6, 20), pricing.getDealerOfferDate());
    }
}
