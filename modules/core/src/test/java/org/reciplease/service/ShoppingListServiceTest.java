package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
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

    private static final String HOUSE_ID = "house-1";

    @Test
    public void shouldGenerateEmptyShoppingList() {
        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), is(empty()));
    }

    @Test
    public void shouldReturnShoppingList() {
        var date = LocalDate.of(2019, 2, 2);

        var recipe = Recipe.builder()
                .name("toast")
                .build()
                .addIngredient("bread", "ITEMS", 10d);
        var plannedRecipe = new PlannedRecipe(HOUSE_ID, recipe, date, List.of());
        when(plannedRecipeRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(plannedRecipe));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), is(recipe.recipeIngredients()));
    }
}
