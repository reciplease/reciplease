package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.IngredientDocument;
import org.reciplease.repository.mongo.IngredientMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class IngredientRepositoryImpl implements IngredientRepository {
    private final IngredientMongoRepository ingredientMongoRepository;

    @Override
    public List<Ingredient> findAll() {
        return ingredientMongoRepository.findAll().stream()
                .map(IngredientDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Ingredient> findByUuid(final UUID uuid) {
        return ingredientMongoRepository.findById(uuid).map(IngredientDocument::toModel);
    }

    @Override
    public Ingredient save(final Ingredient ingredient) {
        return ingredientMongoRepository.save(IngredientDocument.from(ingredient)).toModel();
    }

    @Override
    public List<Ingredient> saveAll(final List<Ingredient> ingredients) {
        return ingredientMongoRepository.saveAll(
                ingredients.stream().map(IngredientDocument::from).collect(Collectors.toList())
        ).stream().map(IngredientDocument::toModel).collect(Collectors.toList());
    }

    @Override
    public List<Ingredient> searchByName(final String searchName) {
        return ingredientMongoRepository.findByNameContainingIgnoreCase(searchName).stream()
                .map(IngredientDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUuid(final UUID uuid) {
        ingredientMongoRepository.deleteById(uuid);
    }
}
