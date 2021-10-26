package org.reciplease.repository;

import org.reciplease.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
    List<Ingredient> findByNameContains(String searchName);
}
