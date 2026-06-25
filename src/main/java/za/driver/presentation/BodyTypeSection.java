package za.driver.presentation;

import java.util.List;

public record BodyTypeSection(String label, List<ModelGroup> models) {

    public BodyTypeSection {
        models = List.copyOf(models);
    }
}
