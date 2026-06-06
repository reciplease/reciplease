package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@AllArgsConstructor
@Builder
public class PlannedRecipe {
    @NonNull
    Recipe recipe;
    @NonNull
    LocalDate date;
    @Singular
    List<IngredientPairing> pairings;
}
