package org.reciplease.model;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder(toBuilder = true)
public class RecipeIngredient extends BaseModel {
    @NonNull
    Recipe recipe;

    @ToString.Include
    @NonNull
    Ingredient ingredient;

    @ToString.Include
    @NonNull
    Double amount;
}
