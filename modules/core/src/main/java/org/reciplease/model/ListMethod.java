package org.reciplease.model;

import java.util.List;

public record ListMethod(List<String> steps) implements RecipeMethod {
    public ListMethod {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
