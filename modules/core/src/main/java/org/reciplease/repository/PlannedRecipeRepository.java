package org.reciplease.repository;

import org.reciplease.model.PlannedRecipe;

import java.time.LocalDate;
import java.util.List;

public interface PlannedRecipeRepository {
    PlannedRecipe save(PlannedRecipe plannedRecipe);
    List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end);
}
