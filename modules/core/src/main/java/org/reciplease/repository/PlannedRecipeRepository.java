package org.reciplease.repository;

import org.reciplease.model.PlannedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlannedRecipeRepository extends JpaRepository<PlannedRecipe, UUID> {
    List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end);
}
