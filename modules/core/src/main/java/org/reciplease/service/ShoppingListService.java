package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.ShoppingList;
import org.reciplease.repository.PlannedRecipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShoppingListService {
    private final PlannedRecipeRepository plannedRecipeRepository;

    public ShoppingList generateShoppingList(final LocalDate start, final LocalDate end) {
        final Set<RecipeIngredient> recipeIngredients = plannedRecipeRepository.findByDateIsBetween(start, end).stream()
                .map(PlannedRecipe::getRecipe)
                .flatMap(recipe -> recipe.getRecipeIngredients().stream())
                .collect(Collectors.toSet());
        return new ShoppingList(recipeIngredients);
    }
}
