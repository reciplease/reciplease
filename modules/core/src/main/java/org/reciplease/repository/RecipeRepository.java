package org.reciplease.repository;

import org.reciplease.model.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    List<Recipe> findAll();

    Recipe save(Recipe recipe);

    Optional<Recipe> findById(String id);

    void deleteById(String id);
}
