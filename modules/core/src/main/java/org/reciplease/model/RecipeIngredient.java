package org.reciplease.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A self-contained description of what a recipe needs: a named ingredient, the unit it is
 * measured in, and the amount required. Deliberately holds no reference to inventory — an
 * {@link InventoryItem} is only paired with a recipe ingredient when a recipe is planned.
 */
@Value
@Builder
public class RecipeIngredient {
    @NonNull
    String name;

    @NonNull
    String measure;

    @NonNull
    Double amount;
}
