package org.reciplease.model;

import java.util.Objects;

/**
 * A self-contained description of what a recipe needs: a named ingredient, the unit it is
 * measured in, and the amount required. Deliberately holds no reference to inventory — an
 * {@link InventoryItem} is only paired with a recipe ingredient when a recipe is planned.
 */
public record RecipeIngredient(String name, String measure, Double amount) {
    public RecipeIngredient {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(measure, "measure");
        Objects.requireNonNull(amount, "amount");
    }
}
