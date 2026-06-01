package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeDocument;
import org.reciplease.repository.mongo.RecipeMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepository {
    private final RecipeMongoRepository recipeMongoRepository;

    @Override
    public List<Recipe> findAll() {
        return recipeMongoRepository.findAll().stream()
                .map(RecipeDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Recipe save(final Recipe recipe) {
        return recipeMongoRepository.save(RecipeDocument.from(recipe)).toModel();
    }

    @Override
    public Optional<Recipe> findById(final String id) {
        return recipeMongoRepository.findById(id).map(RecipeDocument::toModel);
    }

    @Override
    public void deleteById(final String id) {
        recipeMongoRepository.deleteById(id);
    }
}
