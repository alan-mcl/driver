package za.driver.chart;

import za.driver.model.Vehicle;

public record PriceDiscoveryCrossover(
        Vehicle subject,
        String subjectLabel,
        double listPrice,
        Double dealerOffer,
        double listScorePer100k,
        double crossoverPrice,
        double crossoverScorePer100k,
        double discountAmount,
        double discountPct,
        boolean beatsAtList) {
}
