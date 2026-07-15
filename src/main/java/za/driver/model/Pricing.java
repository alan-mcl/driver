package za.driver.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pricing {

    @JsonProperty("listPrice")
    @JsonAlias({"listPriceZar", "priceZar"})
    private BigDecimal listPrice;
    @JsonProperty("dealerOffer")
    @JsonAlias("dealerOfferZar")
    private BigDecimal dealerOffer;
    @JsonAlias("priceDate")
    private LocalDate listPriceDate;
    private LocalDate dealerOfferDate;

    public Pricing() {
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public void setListPrice(BigDecimal listPrice) {
        this.listPrice = listPrice;
    }

    public BigDecimal getDealerOffer() {
        return dealerOffer;
    }

    public void setDealerOffer(BigDecimal dealerOffer) {
        this.dealerOffer = dealerOffer;
    }

    public LocalDate getListPriceDate() {
        return listPriceDate;
    }

    public void setListPriceDate(LocalDate listPriceDate) {
        this.listPriceDate = listPriceDate;
    }

    public LocalDate getDealerOfferDate() {
        return dealerOfferDate;
    }

    public void setDealerOfferDate(LocalDate dealerOfferDate) {
        this.dealerOfferDate = dealerOfferDate;
    }

    public BigDecimal effectivePrice() {
        if (dealerOffer != null && dealerOffer.signum() > 0) {
            return dealerOffer;
        }
        return listPrice;
    }

    public BigDecimal filterPrice() {
        boolean hasList = listPrice != null && listPrice.signum() > 0;
        boolean hasDealer = dealerOffer != null && dealerOffer.signum() > 0;
        if (hasList && hasDealer) {
            return listPrice.min(dealerOffer);
        }
        if (hasDealer) {
            return dealerOffer;
        }
        return listPrice;
    }

    public void normalizeDates() {
        if (dealerOffer != null && dealerOffer.signum() > 0
                && dealerOfferDate == null && listPriceDate != null) {
            dealerOfferDate = listPriceDate;
        }
    }
}
