package com.reciplease.repository;

import com.reciplease.model.PlannedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "planned_recipe", path = "planned_recipe")
public interface PlannedRecipeRepository extends JpaRepository<PlannedRecipe, String> {
}
