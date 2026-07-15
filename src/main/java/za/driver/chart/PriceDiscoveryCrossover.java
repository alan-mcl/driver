package za.driver.chart;

import za.driver.model.Vehicle;

public record PriceDiscoveryCrossover(
        Vehicle subject,
        String subjectLabel,
        double listPrice,
        double listScorePer100k,
        double crossoverPrice,
        double crossoverScorePer100k,
        double discountZar,
        double discountPct,
        boolean beatsAtList) {
}
