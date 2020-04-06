package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeItem;
import org.reciplease.model.ShoppingList;
import org.reciplease.repository.PlannedRecipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShoppingListService {
    private final PlannedRecipeRepository plannedRecipeRepository;

    public ShoppingList generateShoppingList(final LocalDate start, final LocalDate end) {
        final List<RecipeItem> recipeItems = plannedRecipeRepository.findByDateIsBetween(start, end).stream()
                .map(PlannedRecipe::getRecipe)
                .flatMap(recipe -> recipe.getRecipeItems().stream())
                .collect(Collectors.toList());
        return new ShoppingList(recipeItems);
    }
}
