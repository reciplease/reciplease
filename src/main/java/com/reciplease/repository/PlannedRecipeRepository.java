package com.reciplease.repository;

import com.reciplease.model.PlannedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;

@RepositoryRestResource(collectionResourceRel = "planned_recipe", path = "planned_recipe")
public interface PlannedRecipeRepository extends JpaRepository<PlannedRecipe, String> {
    List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end);
}
