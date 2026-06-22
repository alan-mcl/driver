package za.driver.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import za.driver.model.Aspiration;
import za.driver.model.Engine;
import za.driver.model.FuelType;
import za.driver.model.Transmission;
import za.driver.model.TransmissionType;

class PowertrainReliabilityScorerTest {

    private final PowertrainReliabilityScorer scorer = new PowertrainReliabilityScorer();

    @Test
    void score_naManualPetrol_appliesStackedAdjustments() {
        Engine engine = engine(FuelType.PETROL, Aspiration.NATURALLY_ASPIRATED, false, false);
        Transmission transmission = transmission(TransmissionType.MANUAL);

        assertEquals(90.0, scorer.score(engine, transmission));
    }

    @Test
    void score_turboAutomaticPetrol_appliesAdjustments() {
        Engine engine = engine(FuelType.PETROL, Aspiration.TURBOCHARGED, false, false);
        Transmission transmission = transmission(TransmissionType.AUTOMATIC);

        assertEquals(77.0, scorer.score(engine, transmission));
    }

    @Test
    void score_phev_skipsAspirationAdjustments() {
        Engine engine = engine(FuelType.PHEV, Aspiration.TURBOCHARGED, false, true);
        Transmission transmission = transmission(TransmissionType.AUTOMATIC);

        assertEquals(70.0, scorer.score(engine, transmission));
    }

    @Test
    void score_ev_appliesEvAdjustment() {
        Engine engine = engine(FuelType.EV, null, false, false);

        assertEquals(80.0, scorer.score(engine, null));
    }

    @Test
    void score_missingEngineInputs_returnsNull() {
        Engine engine = new Engine();
        assertNull(scorer.score(engine, transmission(TransmissionType.MANUAL)));
    }

    private static Engine engine(FuelType fuelType, Aspiration aspiration, boolean hybrid, boolean phev) {
        Engine engine = new Engine();
        engine.setFuelType(fuelType);
        engine.setAspiration(aspiration);
        engine.setHybrid(hybrid);
        engine.setPhev(phev);
        return engine;
    }

    private static Transmission transmission(TransmissionType type) {
        Transmission transmission = new Transmission();
        transmission.setType(type);
        return transmission;
    }
}
