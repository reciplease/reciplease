package org.reciplease.repository;

import org.reciplease.model.PlannedRecipe;

import java.time.LocalDate;
import java.util.List;

// extends JpaRepository<PlannedRecipe, UUID>
public interface PlannedRecipeRepository {
    List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end);
}
