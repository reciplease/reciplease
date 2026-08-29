package org.reciplease.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlannedMealTest {

    @Test
    void requiresName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlannedMeal("house-1", null, null, LocalDate.now(), List.of()));
    }

    @Test
    void requiresNonBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlannedMeal("house-1", null, "   ", LocalDate.now(), List.of()));
    }

    @Test
    void nameRequiredEvenWhenRecipeIsSet() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlannedMeal("house-1", "recipe-1", null, LocalDate.now(), List.of()));
    }
}
