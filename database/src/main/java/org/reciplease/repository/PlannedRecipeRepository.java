package org.reciplease.repository;

import org.reciplease.model.PlannedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedRecipeRepository extends JpaRepository<PlannedRecipe, String> {
    List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end);
}
