package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.PlannedRecipeDocument;
import org.reciplease.repository.mongo.PlannedRecipeMongoRepository;
import org.reciplease.repository.mongo.RecipeMongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlannedRecipeRepositoryImpl implements PlannedRecipeRepository {
    private final PlannedRecipeMongoRepository plannedRecipeMongoRepository;
    private final RecipeMongoRepository recipeMongoRepository;

    @Override
    public PlannedRecipe save(final PlannedRecipe plannedRecipe) {
        final var saved = plannedRecipeMongoRepository.save(PlannedRecipeDocument.from(plannedRecipe));
        return saved.toModel(plannedRecipe.getRecipe());
    }

    @Override
    public List<PlannedRecipe> findByDateIsBetween(final LocalDate start, final LocalDate end) {
        return plannedRecipeMongoRepository.findByDateBetween(start, end).stream()
                .flatMap(doc -> recipeMongoRepository.findById(doc.getRecipeId())
                        .map(recipe -> doc.toModel(recipe.toModel()))
                        .stream())
                .collect(Collectors.toList());
    }
}
