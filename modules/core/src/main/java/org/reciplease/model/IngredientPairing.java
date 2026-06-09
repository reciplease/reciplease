package org.reciplease.model;

import java.util.List;
import java.util.Objects;

/**
 * Links a single {@link RecipeIngredient} to the {@link InventoryItem}s chosen to satisfy it
 * when a recipe is planned. More than one allocation is allowed because a single inventory item
 * may not cover the whole required amount.
 */
public record IngredientPairing(RecipeIngredient recipeIngredient, List<InventoryAllocation> allocations) {
    public IngredientPairing {
        Objects.requireNonNull(recipeIngredient, "recipeIngredient");
        allocations = allocations == null ? List.of() : List.copyOf(allocations);
    }
}
