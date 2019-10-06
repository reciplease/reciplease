package org.reciplease.repository;

import org.reciplease.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

public interface RecipeRepository extends JpaRepository<Recipe, String> {
}
