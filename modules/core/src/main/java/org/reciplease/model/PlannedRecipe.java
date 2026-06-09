package org.reciplease.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record PlannedRecipe(Recipe recipe, LocalDate date, List<IngredientPairing> pairings) {
    public PlannedRecipe {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(date, "date");
        pairings = pairings == null ? List.of() : List.copyOf(pairings);
    }
}
