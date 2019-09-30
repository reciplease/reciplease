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
        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(start, end);
        final List<Recipe> recipes = getRecipes(plannedRecipes);
        final List<RecipeItem> recipeItems = getItemsFromRecipes(recipes);
        return new ShoppingList(recipeItems);
    }

    private List<Recipe> getRecipes(final List<PlannedRecipe> plannedRecipes) {
        return plannedRecipes.stream().map(PlannedRecipe::getRecipe).collect(Collectors.toList());
    }

    private List<RecipeItem> getItemsFromRecipes(final List<Recipe> recipes) {
        return recipes.stream().flatMap(recipe -> recipe.getRecipeItems().stream()).collect(Collectors.toList());
    }
}
