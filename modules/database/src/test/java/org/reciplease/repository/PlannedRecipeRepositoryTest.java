package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
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
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import({PlannedRecipeRepositoryImpl.class, RecipeRepositoryImpl.class, MongoAuditingConfig.class})
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
        var recipe = recipeRepository.save(Recipe.builder().build());
        var plannedRecipe = plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 2), List.of()));

        var plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipe));
    }

    @Test
    public void shouldReturnEmptyList() {
        var recipe = recipeRepository.save(Recipe.builder().build());
        plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 5), List.of()));

        var plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }

    @Test
    public void shouldSetIdAndCreatedAtAndUpdatedAtOnSave() {
        var recipe = recipeRepository.save(Recipe.builder().build());

        var saved = plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2026, 6, 10), List.of()));

        assertThat(saved.id(), is(notNullValue()));
        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    public void shouldRoundTripPairingsAndFindByRecipeId() {
        var recipe = recipeRepository.save(Recipe.builder().name("toast").build());
        var pairing = new IngredientPairing(
                new RecipeIngredient("bread", "ITEMS", 2d),
                List.of(new InventoryAllocation("item-1", "111", 2d)));
        plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2026, 6, 6), List.of(pairing)));

        var found = plannedRecipeRepository.findByRecipeId(recipe.id());

        assertThat(found.size(), is(1));
        assertThat(found.get(0).pairings(), contains(pairing));
    }
}
