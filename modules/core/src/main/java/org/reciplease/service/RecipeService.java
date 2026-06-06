package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.RecipeRepository;
import org.reciplease.service.request.AddIngredient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public Recipe create(final Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public void deleteById(final String id) {
        recipeRepository.deleteById(id);
    }

    public Set<RecipeIngredient> addIngredient(final String recipeId, final AddIngredient addIngredient) {
        final var recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));

        recipe.addIngredient(addIngredient.getName(), addIngredient.getMeasure(), addIngredient.getAmount());

        final var savedRecipe = recipeRepository.save(recipe);

        return savedRecipe.getRecipeIngredients();
    }
}
