package org.reciplease.model;

import java.util.List;
import java.util.Objects;

/**
 * A single ingredient placeholder for a planned meal, optionally covered by pantry
 * allocations. An empty {@link #allocations} list — or one that doesn't fully cover
 * {@code ingredient.amount()} — means the remainder is wanted but not yet owned, which is
 * exactly what feeds the shopping list gap.
 */
public record PlannedIngredient(RecipeIngredient ingredient, List<PantryAllocation> allocations) {
    public PlannedIngredient {
        Objects.requireNonNull(ingredient, "ingredient");
        allocations = allocations == null ? List.of() : List.copyOf(allocations);
    }
}
