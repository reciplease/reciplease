package org.reciplease.repository;

import org.reciplease.model.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository {
    List<Ingredient> findAll();

    Optional<Ingredient> findById(String id);

    Ingredient save(Ingredient ingredient);

    List<Ingredient> saveAll(List<Ingredient> ingredients);

    List<Ingredient> searchByName(String searchName);

    void deleteById(String id);
}
