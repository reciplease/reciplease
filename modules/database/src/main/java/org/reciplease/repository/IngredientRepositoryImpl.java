package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.IngredientEntity;
import org.reciplease.repository.jpa.IngredientJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryImpl implements IngredientRepository {
    private final IngredientJpaRepository ingredientJpaRepository;

    @Override
    public List<Ingredient> findAll() {
        return ingredientJpaRepository.findAll().stream()
            .map(IngredientEntity::toModel)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Ingredient> findById(UUID uuid) {
        return ingredientJpaRepository.findById(uuid).map(IngredientEntity::toModel);
    }

    @Override
    public Ingredient save(Ingredient ingredient) {
        return ingredientJpaRepository.save(IngredientEntity.from(ingredient)).toModel();
    }

    @Override
    public List<Ingredient> saveAll(List<Ingredient> ingredients) {
        final var ingredientEntities = ingredients.stream()
            .map(IngredientEntity::from)
            .collect(Collectors.toList());
        return ingredientJpaRepository.saveAll(ingredientEntities).stream()
            .map(IngredientEntity::toModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<Ingredient> searchByName(String searchName) {
        return ingredientJpaRepository.findByNameContains(searchName).stream()
            .map(IngredientEntity::toModel)
            .collect(Collectors.toList());
    }
}
