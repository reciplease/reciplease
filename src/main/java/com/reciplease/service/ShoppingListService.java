package com.reciplease.service;

import com.reciplease.model.PlannedRecipe;
import com.reciplease.model.Recipe;
import com.reciplease.model.RecipeItem;
import com.reciplease.model.ShoppingList;
import com.reciplease.repository.PlannedRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShoppingListService {
    final PlannedRecipeRepository plannedRecipeRepository;

    public ShoppingList generateShoppingList(LocalDate start, LocalDate end) {
        List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(start, end);
        List<Recipe> recipes = getRecipes(plannedRecipes);
        List<RecipeItem> recipeItems = getItemsFromRecipes(recipes);
        return new ShoppingList(recipeItems);
    }

    private List<Recipe> getRecipes(List<PlannedRecipe> plannedRecipes) {
        return plannedRecipes.stream().map(PlannedRecipe::getRecipe).collect(Collectors.toList());
    }

    private List<RecipeItem> getItemsFromRecipes(List<Recipe> recipes) {
        return recipes.stream().flatMap(recipe -> recipe.getRecipeItems().stream()).collect(Collectors.toList());
    }
}
