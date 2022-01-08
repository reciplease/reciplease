package org.reciplease.model;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
public class RecipeIngredient {
    @NonNull
    Recipe recipe;

    @ToString.Include
    @NonNull
    Ingredient ingredient;

    @ToString.Include
    @NonNull
    Double amount;
}
