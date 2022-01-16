package org.reciplease.repository;

import org.reciplease.model.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository {
    List<Ingredient> findAll();
    Optional<Ingredient> findByUuid(final UUID uuid);
    Ingredient save(final Ingredient ingredient);
    List<Ingredient> saveAll(final List<Ingredient> ingredients);
    List<Ingredient> searchByName(String searchName);
}
