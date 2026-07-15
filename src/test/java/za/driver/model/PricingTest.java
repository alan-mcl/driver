package za.driver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PricingTest {

    @Test
    void effectivePriceZar_prefersDealerOfferWhenSet() {
        Pricing pricing = new Pricing();
        pricing.setListPriceZar(new BigDecimal("350000"));
        pricing.setDealerOfferZar(new BigDecimal("320000"));

        assertEquals(new BigDecimal("320000"), pricing.effectivePriceZar());
    }

    @Test
    void effectivePriceZar_fallsBackToListPrice() {
        Pricing pricing = new Pricing();
        pricing.setListPriceZar(new BigDecimal("350000"));

        assertEquals(new BigDecimal("350000"), pricing.effectivePriceZar());
    }

    @Test
    void effectivePriceZar_ignoresNonPositiveDealerOffer() {
        Pricing pricing = new Pricing();
        pricing.setListPriceZar(new BigDecimal("350000"));
        pricing.setDealerOfferZar(BigDecimal.ZERO);

        assertEquals(new BigDecimal("350000"), pricing.effectivePriceZar());
    }

    @Test
    void effectivePriceZar_returnsNullWhenNoPrices() {
        Pricing pricing = new Pricing();

        assertNull(pricing.effectivePriceZar());
    }
}
