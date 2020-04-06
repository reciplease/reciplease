package org.reciplease.repository;

import org.reciplease.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, String> {
}
