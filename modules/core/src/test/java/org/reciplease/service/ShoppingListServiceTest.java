package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.PlannedMealRepository;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@MockitoSettings
public class ShoppingListServiceTest {
    private final LocalDate startDate = LocalDate.of(2019, 2, 1);
    private final LocalDate endDate = LocalDate.of(2019, 2, 3);
    private final LocalDate date = LocalDate.of(2019, 2, 2);

    @Mock
    private PlannedMealRepository plannedMealRepository;

    private ShoppingListService shoppingListService;

    private static final String HOUSE_ID = "house-1";

    @BeforeEach
    public void setUp() {
        shoppingListService = new ShoppingListService(plannedMealRepository);
    }

    @Test
    public void shouldGenerateEmptyShoppingList() {
        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), is(empty()));
    }

    @Test
    public void shouldReturnFullAmountWhenNothingAllocated() {
        var plannedIngredient = new PlannedIngredient(new RecipeIngredient("bread", "item", 10d), List.of());
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(plannedIngredient));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), contains(new RecipeIngredient("bread", "item", 10d)));
    }

    @Test
    public void shouldSubtractPartialAllocation() {
        var plannedIngredient = new PlannedIngredient(new RecipeIngredient("bread", "item", 10d),
                List.of(new InventoryAllocation("item-1", "111", 4d)));
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(plannedIngredient));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), contains(new RecipeIngredient("bread", "item", 6d)));
    }

    @Test
    public void shouldOmitFullyAllocatedIngredients() {
        var plannedIngredient = new PlannedIngredient(new RecipeIngredient("bread", "item", 10d),
                List.of(new InventoryAllocation("item-1", "111", 10d)));
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(plannedIngredient));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), is(empty()));
    }

    @Test
    public void shouldNotReturnNegativeGapWhenOverAllocated() {
        var plannedIngredient = new PlannedIngredient(new RecipeIngredient("bread", "item", 10d),
                List.of(new InventoryAllocation("item-1", "111", 15d)));
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(plannedIngredient));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), is(empty()));
    }

    @Test
    public void shouldSumSameIngredientAcrossMultipleMeals() {
        var breakfast = new PlannedIngredient(new RecipeIngredient("bread", "item", 4d), List.of());
        var dinner = new PlannedIngredient(new RecipeIngredient("bread", "item", 6d),
                List.of(new InventoryAllocation("item-1", "111", 2d)));
        var meals = List.of(
                new PlannedMeal(HOUSE_ID, null, "Breakfast", date, List.of(breakfast)),
                new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(dinner)));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(meals);

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        // desired 4 + 6 = 10, covered 2 => gap 8
        assertThat(shoppingList.getItems(), contains(new RecipeIngredient("bread", "item", 8d)));
    }

    @Test
    @DisplayName("combines convertible measures (kg and g) of the same ingredient")
    public void shouldConvertBetweenCompatibleMeasures() {
        var kilogram = new PlannedIngredient(new RecipeIngredient("flour", "kg", 1d), List.of());
        var grams = new PlannedIngredient(new RecipeIngredient("flour", "g", 200d),
                List.of(new InventoryAllocation("item-1", "111", 100d)));
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(kilogram, grams));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        // desired: 1000g + 200g = 1200g, covered: 100g => gap 1100g
        assertThat(shoppingList.getItems(), contains(new RecipeIngredient("flour", "g", 1100d)));
    }

    @Test
    @DisplayName("keeps unrelated measure families (mass vs volume) of the same ingredient separate")
    public void shouldKeepDifferentMeasureFamiliesSeparate() {
        var grams = new PlannedIngredient(new RecipeIngredient("milk", "g", 200d), List.of());
        var millilitres = new PlannedIngredient(new RecipeIngredient("milk", "ml", 300d), List.of());
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(grams, millilitres));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), containsInAnyOrder(
                new RecipeIngredient("milk", "g", 200d),
                new RecipeIngredient("milk", "ml", 300d)));
    }

    @Test
    @DisplayName("keeps unrecognized measure strings separate and unconverted")
    public void shouldKeepUnrecognizedMeasuresSeparate() {
        var grams = new PlannedIngredient(new RecipeIngredient("flour", "g", 200d), List.of());
        var handfuls = new PlannedIngredient(new RecipeIngredient("flour", "handfuls", 2d), List.of());
        var meal = new PlannedMeal(HOUSE_ID, null, "Dinner", date, List.of(grams, handfuls));
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, startDate, endDate)).thenReturn(List.of(meal));

        var shoppingList = shoppingListService.generateShoppingList(HOUSE_ID, startDate, endDate);

        assertThat(shoppingList.getItems(), containsInAnyOrder(
                new RecipeIngredient("flour", "g", 200d),
                new RecipeIngredient("flour", "handfuls", 2d)));
    }
}
