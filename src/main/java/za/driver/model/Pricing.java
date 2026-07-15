package za.driver.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Pricing {

    @JsonAlias("priceZar")
    private BigDecimal listPriceZar;
    private BigDecimal dealerOfferZar;
    private LocalDate priceDate;

    public Pricing() {
    }

    public BigDecimal getListPriceZar() {
        return listPriceZar;
    }

    public void setListPriceZar(BigDecimal listPriceZar) {
        this.listPriceZar = listPriceZar;
    }

    public BigDecimal getDealerOfferZar() {
        return dealerOfferZar;
    }

    public void setDealerOfferZar(BigDecimal dealerOfferZar) {
        this.dealerOfferZar = dealerOfferZar;
    }

    public BigDecimal effectivePriceZar() {
        if (dealerOfferZar != null && dealerOfferZar.signum() > 0) {
            return dealerOfferZar;
        }
        return listPriceZar;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }
}
