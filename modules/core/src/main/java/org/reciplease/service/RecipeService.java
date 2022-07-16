package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.RecipeRepository;
import org.reciplease.service.request.AddIngredient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public Optional<Recipe> findById(final UUID uuid) {
        return recipeRepository.findByUuid(uuid);
    }

    public List<Recipe> findAll() {
        final var all = recipeRepository.findAll();
        log.info("Recipes: {}", all);
        return all;
    }

    public Recipe create(final Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public Set<RecipeIngredient> addIngredient(final UUID recipeUuid, final AddIngredient addIngredient) {
        final var recipe = recipeRepository.findByUuid(recipeUuid)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
        final var ingredient = ingredientRepository.findByUuid(addIngredient.getIngredientUuid())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient does not exist"));

        recipe.addIngredient(ingredient, addIngredient.getAmount());

        final var savedRecipe = recipeRepository.save(recipe);

        return savedRecipe.getRecipeIngredients();
    }
}
