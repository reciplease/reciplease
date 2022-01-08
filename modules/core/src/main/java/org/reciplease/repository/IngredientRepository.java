package org.reciplease.repository;

import org.reciplease.model.Ingredient;

import java.util.List;

// extends JpaRepository<Ingredient, UUID>
public interface IngredientRepository {
    List<Ingredient> findByNameContains(String searchName);
}
