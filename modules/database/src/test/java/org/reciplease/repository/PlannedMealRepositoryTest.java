package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DataMongoTest
@Import({PlannedMealRepositoryImpl.class, RecipeRepositoryImpl.class, MongoAuditingConfig.class})
public class PlannedMealRepositoryTest {
    private static final String HOUSE_ID = "house-1";

    @Autowired
    private PlannedMealRepository plannedMealRepository;
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    public void shouldReturnPlannedMealsByDate() {
        var recipe = recipeRepository.save(Recipe.builder().build());
        var plannedMeal = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2019, 2, 2), List.of()));

        var plannedMeals = plannedMealRepository.findByDateIsBetween(HOUSE_ID, LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedMeals, contains(plannedMeal));
    }

    @Test
    public void shouldIncludeAMealDatedExactlyOnTheRangeEnd() {
        var recipe = recipeRepository.save(Recipe.builder().build());
        var plannedMeal = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2026, 7, 5), List.of()));

        var plannedMeals = plannedMealRepository.findByDateIsBetween(HOUSE_ID, LocalDate.of(2026, 6, 29), LocalDate.of(2026, 7, 5));

        assertThat(plannedMeals, contains(plannedMeal));
    }

    @Test
    public void shouldIncludeAMealDatedExactlyOnTheRangeStart() {
        var recipe = recipeRepository.save(Recipe.builder().build());
        var plannedMeal = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2026, 6, 29), List.of()));

        var plannedMeals = plannedMealRepository.findByDateIsBetween(HOUSE_ID, LocalDate.of(2026, 6, 29), LocalDate.of(2026, 7, 5));

        assertThat(plannedMeals, contains(plannedMeal));
    }

    @Test
    public void shouldReturnEmptyList() {
        var recipe = recipeRepository.save(Recipe.builder().build());
        plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2019, 2, 5), List.of()));

        var plannedMeals = plannedMealRepository.findByDateIsBetween(HOUSE_ID, LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedMeals, is(empty()));
    }

    @Test
    public void shouldSetIdAndCreatedAtAndUpdatedAtOnSave() {
        var recipe = recipeRepository.save(Recipe.builder().build());

        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2026, 6, 10), List.of()));

        assertThat(saved.id(), is(notNullValue()));
        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
        assertThat(saved.eatenAt(), is(nullValue()));
    }

    @Test
    public void shouldRoundTripEatenAt() {
        var recipe = recipeRepository.save(Recipe.builder().build());
        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2026, 6, 10), List.of()));
        var eatenAt = java.time.Instant.parse("2026-06-10T18:30:00Z");

        var marked = plannedMealRepository.save(saved.withEatenAt(eatenAt));

        assertThat(marked.eatenAt(), is(eatenAt));
        assertThat(plannedMealRepository.findById(saved.id()).orElseThrow().eatenAt(), is(eatenAt));
    }

    @Test
    public void shouldRoundTripItemsAndFindByRecipeId() {
        var recipe = recipeRepository.save(Recipe.builder().name("toast").build());
        var item = new PlannedIngredient(
                new RecipeIngredient("bread", "ITEMS", 2d),
                List.of(new InventoryAllocation("item-1", "111", 2d)));
        plannedMealRepository.save(new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", LocalDate.of(2026, 6, 6), List.of(item)));

        var found = plannedMealRepository.findByRecipeId(HOUSE_ID, recipe.id());

        assertThat(found.size(), is(1));
        assertThat(found.get(0).items(), contains(item));
    }

    @Test
    public void shouldRoundTripMealsWithoutARecipe() {
        var item = new PlannedIngredient(new RecipeIngredient("rice", "GRAMS", 200d), List.of());
        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Leftover rice night", LocalDate.of(2026, 6, 6), List.of(item)));

        var found = plannedMealRepository.findByDateIsBetween(HOUSE_ID, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThat(found, contains(saved));
        assertThat(found.get(0).recipeId(), is(nullValue()));
    }

    @Test
    public void shouldFindByIngredientName() {
        var item = new PlannedIngredient(new RecipeIngredient("bread", "ITEMS", 2d),
                List.of(new InventoryAllocation("item-1", "111", 2d)));
        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of(item)));

        var found = plannedMealRepository.findByIngredientName(HOUSE_ID, "bread");

        assertThat(found, contains(saved));
    }

    @Test
    public void shouldReportExistingNameOnThatDate() {
        plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of()));

        assertThat(plannedMealRepository.existsByHouseIdAndDateAndName(HOUSE_ID, LocalDate.of(2026, 6, 6), "Dinner"), is(true));
        assertThat(plannedMealRepository.existsByHouseIdAndDateAndName(HOUSE_ID, LocalDate.of(2026, 6, 7), "Dinner"), is(false));
        assertThat(plannedMealRepository.existsByHouseIdAndDateAndName(HOUSE_ID, LocalDate.of(2026, 6, 6), "Lunch"), is(false));
    }

    @Test
    public void shouldFindById() {
        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of()));

        assertThat(plannedMealRepository.findById(saved.id()), is(java.util.Optional.of(saved)));
        assertThat(plannedMealRepository.findById("does-not-exist"), is(java.util.Optional.empty()));
    }

    @Test
    public void shouldDeleteById() {
        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of()));

        plannedMealRepository.deleteById(saved.id());

        assertThat(plannedMealRepository.findById(saved.id()), is(java.util.Optional.empty()));
    }

    @Test
    public void shouldReportExistingNameOnThatDateExcludingGivenId() {
        var saved = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of()));

        assertThat(plannedMealRepository.existsByHouseIdAndDateAndNameAndIdNot(HOUSE_ID, LocalDate.of(2026, 6, 6), "Dinner", saved.id()), is(false));

        var other = plannedMealRepository.save(new PlannedMeal(HOUSE_ID, null, "Lunch", LocalDate.of(2026, 6, 6), List.of()));
        assertThat(plannedMealRepository.existsByHouseIdAndDateAndNameAndIdNot(HOUSE_ID, LocalDate.of(2026, 6, 6), "Dinner", other.id()), is(true));
    }
}
