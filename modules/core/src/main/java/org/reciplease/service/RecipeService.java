package org.reciplease.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.RecipeRepository;
import org.reciplease.service.request.AddIngredient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public Optional<Recipe> findById(final String id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> findAll() {
        final var all = recipeRepository.findAll();
        log.info("Recipes: {}", all);
        return all;
    }

    /**
     * Public recipes plus, if {@code houseId} is non-null, private recipes belonging to
     * that house.
     */
    public List<Recipe> findVisibleTo(final String houseId) {
        return recipeRepository.findVisibleTo(houseId);
    }

    public Optional<Recipe> findVisibleById(final String id, final String houseId) {
        return recipeRepository.findVisibleById(id, houseId);
    }

    public Recipe create(final String houseId, final Recipe recipe) {
        return recipeRepository.save(recipe.toBuilder().houseId(houseId).build());
    }

    public Recipe update(final String id, final Recipe updates) {
        final var existing =
                recipeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));

        final var merged = existing.toBuilder()
                .name(updates.name())
                .description(updates.description())
                .steps(updates.steps())
                .recipeIngredients(updates.recipeIngredients())
                .isPublic(updates.isPublic())
                .build();

        return recipeRepository.save(merged);
    }

    public void deleteById(final String id) {
        recipeRepository.deleteById(id);
    }

    public Set<RecipeIngredient> addIngredient(final String recipeId, final AddIngredient addIngredient) {
        final var recipe = recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));

        recipe.addIngredient(addIngredient.getName(), addIngredient.getMeasure(), addIngredient.getAmount());

        final var savedRecipe = recipeRepository.save(recipe);

        return savedRecipe.recipeIngredients();
    }
}
