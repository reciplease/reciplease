package com.reciplease.service;

import com.reciplease.model.PlannedRecipe;
import com.reciplease.model.Recipe;
import com.reciplease.model.RecipeItem;
import com.reciplease.model.ShoppingList;
import com.reciplease.repository.PlannedRecipeRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.doReturn;

public class ShoppingListServiceTest {
    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;
    @Mock
    private Recipe recipe;
    @Mock
    private RecipeItem recipeItem;

    private ShoppingListService shoppingListService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        shoppingListService = new ShoppingListService(plannedRecipeRepository);
    }

    @Test
    public void shouldGenerateEmptyShoppingList() {
        final ShoppingList shoppingList = shoppingListService.generateShoppingList(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(shoppingList.getItems(), is(empty()));
    }

    @Test
    public void shouldReturnShoppingList() {
        final List<RecipeItem> recipeItems = Collections.singletonList(recipeItem);
        doReturn(recipeItems).when(recipe).getRecipeItems();
        final List<PlannedRecipe> plannedRecipes = Collections.singletonList(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 2)));
        doReturn(plannedRecipes).when(plannedRecipeRepository).findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        final ShoppingList shoppingList = shoppingListService.generateShoppingList(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(shoppingList.getItems(), is(recipeItems));
    }
}