package org.reciplease.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record PlannedRecipe(
        String id,
        String createdBy,
        Recipe recipe,
        LocalDate date,
        List<IngredientPairing> pairings,
        Instant createdAt,
        Instant updatedAt) implements Audited {

    public PlannedRecipe {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(date, "date");
        pairings = pairings == null ? List.of() : List.copyOf(pairings);
    }

    public PlannedRecipe(final Recipe recipe, final LocalDate date, final List<IngredientPairing> pairings) {
        this(null, null, recipe, date, pairings, null, null);
    }
}
