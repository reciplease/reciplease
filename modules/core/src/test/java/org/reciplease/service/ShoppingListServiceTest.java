package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.ShoppingList;
import org.reciplease.repository.PlannedRecipeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class ShoppingListServiceTest {
    private final LocalDate startDate = LocalDate.of(2019, 2, 1);
    private final LocalDate endDate = LocalDate.of(2019, 2, 3);

    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;

    @Mock
    private Recipe recipe;
    @Mock
    private RecipeIngredient recipeIngredient;

    private ShoppingListService shoppingListService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        shoppingListService = new ShoppingListService(plannedRecipeRepository);
    }

    @Test
    public void shouldGenerateEmptyShoppingList() {
        final ShoppingList shoppingList = shoppingListService.generateShoppingList(startDate, endDate);

        assertThat(shoppingList.getItems(), is(empty()));
    }

    @Test
    public void shouldReturnShoppingList() {
        final LocalDate date = LocalDate.of(2019, 2, 2);

        final Set<RecipeIngredient> recipeIngredients = Set.of(recipeIngredient);
        final List<PlannedRecipe> plannedRecipes = List.of(new PlannedRecipe(recipe, date));

        when(recipe.getRecipeIngredients()).thenReturn(recipeIngredients);
        when(plannedRecipeRepository.findByDateIsBetween(startDate, endDate)).thenReturn(plannedRecipes);

        final ShoppingList shoppingList = shoppingListService.generateShoppingList(startDate, endDate);

        assertThat(shoppingList.getItems(), is(recipeIngredients));
    }
}