package org.reciplease.model;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
public class RecipeIngredient {
    @ToString.Include
    @NonNull
    Ingredient ingredient;

    @ToString.Include
    @NonNull
    Double amount;
}
