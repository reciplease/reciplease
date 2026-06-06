package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.IngredientPairing;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.PlannedRecipe;
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

@DataMongoTest
@Import({PlannedRecipeRepositoryImpl.class, RecipeRepositoryImpl.class})
public class PlannedRecipeRepositoryTest {
    @Autowired
    private PlannedRecipeRepository plannedRecipeRepository;
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    public void shouldReturnPlannedRecipesByDate() {
        final Recipe recipe = recipeRepository.save(Recipe.builder().build());
        final PlannedRecipe plannedRecipe = plannedRecipeRepository.save(PlannedRecipe.builder()
                .recipe(recipe).date(LocalDate.of(2019, 2, 2)).build());

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipe));
    }

    @Test
    public void shouldReturnEmptyList() {
        final Recipe recipe = recipeRepository.save(Recipe.builder().build());
        plannedRecipeRepository.save(PlannedRecipe.builder()
                .recipe(recipe).date(LocalDate.of(2019, 2, 5)).build());

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }

    @Test
    public void shouldRoundTripPairingsAndFindByRecipeId() {
        final Recipe recipe = recipeRepository.save(Recipe.builder().name("toast").build());
        final var pairing = IngredientPairing.builder()
                .recipeIngredient(RecipeIngredient.builder().name("bread").measure("ITEMS").amount(2d).build())
                .allocation(InventoryAllocation.builder()
                        .inventoryItemId("item-1").barcode("111").amount(2d).build())
                .build();
        plannedRecipeRepository.save(PlannedRecipe.builder()
                .recipe(recipe).date(LocalDate.of(2026, 6, 6)).pairing(pairing).build());

        final List<PlannedRecipe> found = plannedRecipeRepository.findByRecipeId(recipe.getId());

        assertThat(found, contains(is(org.hamcrest.Matchers.hasProperty("pairings", contains(pairing)))));
    }
}
