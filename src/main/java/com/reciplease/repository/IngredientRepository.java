package com.reciplease.repository;

import com.reciplease.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "ingredients", path = "ingredients")
public interface IngredientRepository extends JpaRepository<Ingredient, String> {
}
