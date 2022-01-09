package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.IngredientEntity;
import org.reciplease.repository.jpa.IngredientJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryImpl implements IngredientRepository {
    private final IngredientJpaRepository ingredientJpaRepository;

    @Override
    public List<Ingredient> findAll() {
        return null;
    }

    @Override
    public Optional<Ingredient> findById(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Ingredient save(Ingredient ingredient) {
        return ingredientJpaRepository.save(IngredientEntity.from(ingredient)).toModel();
    }

    @Override
    public List<Ingredient> saveAll(List<Ingredient> ingredients) {
        return null;
    }

    @Override
    public List<Ingredient> findByNameContains(String searchName) {
        return null;
    }
}
