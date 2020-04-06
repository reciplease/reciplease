package org.reciplease.repository;

import org.reciplease.model.Ingredient;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, String> {
    List<Ingredient> findByNameContains(String searchName);

    @Override
    default <S extends Ingredient> boolean exists(final Example<S> example) {
        return false;
    }
}
