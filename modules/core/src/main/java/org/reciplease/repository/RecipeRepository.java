package org.reciplease.repository;

import org.reciplease.model.Recipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository {
    List<Recipe> findAll();

    Recipe save(final Recipe recipe);

    Optional<Recipe> findById(final UUID recipeId);
}
