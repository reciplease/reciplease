package org.reciplease.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * Links a single {@link RecipeIngredient} to the {@link InventoryItem}s chosen to satisfy it
 * when a recipe is planned. More than one allocation is allowed because a single inventory item
 * may not cover the whole required amount.
 */
@Value
@Builder
public class IngredientPairing {
    @NonNull
    RecipeIngredient recipeIngredient;

    @Singular
    List<InventoryAllocation> allocations;
}
