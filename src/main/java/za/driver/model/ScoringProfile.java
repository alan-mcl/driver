package za.driver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoringProfile {

    private UUID id;
    private String name;
    private List<ScoringWeight> weights;
    private String aggregateName;
    private List<ScoringWeight> aggregateComponents;

    public ScoringProfile() {
        this.weights = new ArrayList<>();
        this.aggregateComponents = new ArrayList<>();
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

    public String getAggregateName() {
        return aggregateName;
    }

    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    public List<ScoringWeight> getAggregateComponents() {
        return aggregateComponents;
    }

    public void setAggregateComponents(List<ScoringWeight> aggregateComponents) {
        this.aggregateComponents = aggregateComponents;
    }
}
