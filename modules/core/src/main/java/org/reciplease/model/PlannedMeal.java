package org.reciplease.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record PlannedMeal(
        String id,
        String createdBy,
        String houseId,
        String recipeId,
        String name,
        LocalDate date,
        List<PlannedIngredient> items,
        Instant createdAt,
        Instant updatedAt) implements Audited, HouseScoped {

    public PlannedMeal {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        Objects.requireNonNull(date, "date");
        items = items == null ? List.of() : List.copyOf(items);
    }

    public PlannedMeal(final String houseId, final String recipeId, final String name,
                        final LocalDate date, final List<PlannedIngredient> items) {
        this(null, null, houseId, recipeId, name, date, items, null, null);
    }
}
