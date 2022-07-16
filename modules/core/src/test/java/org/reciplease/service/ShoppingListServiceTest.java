package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.Ingredient;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.reciplease.model.ShoppingList;
import org.reciplease.repository.PlannedRecipeRepository;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@MockitoSettings
public class ShoppingListServiceTest {
    private final LocalDate startDate = LocalDate.of(2019, 2, 1);
    private final LocalDate endDate = LocalDate.of(2019, 2, 3);

    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;

    private ShoppingListService shoppingListService;

    @BeforeEach
    public void setUp() {
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

        final var bread = Ingredient.builder()
                .name("bread").build();
        final var recipe = Recipe.builder()
                .name("toast")
                .build()
                .addIngredient(bread, 10d);
        PlannedRecipe plannedRecipe = PlannedRecipe.builder()
                .recipe(recipe)
                .date(date).build();
        final List<PlannedRecipe> plannedRecipes = List.of(plannedRecipe);
        when(plannedRecipeRepository.findByDateIsBetween(startDate, endDate)).thenReturn(plannedRecipes);

        final ShoppingList shoppingList = shoppingListService.generateShoppingList(startDate, endDate);

        assertThat(shoppingList.getItems(), is(recipe.getRecipeIngredients()));
    }
}
