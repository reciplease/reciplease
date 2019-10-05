package org.reciplease.repository;

import org.reciplease.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

public interface IngredientRepository extends JpaRepository<Ingredient, String> {
}
