package za.driver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoringProfile {

    private UUID id;
    private String name;
    private List<ScoringWeight> weights;

    public ScoringProfile() {
        this.weights = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScoringWeight> getWeights() {
        return weights;
    }

    public void setWeights(List<ScoringWeight> weights) {
        this.weights = weights;
    }
}
