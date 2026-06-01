package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .build());
        final PlannedRecipe plannedRecipe = plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 2)));

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipe));
    }

    @Test
    public void shouldReturnEmptyList() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .build());
        plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 5)));

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }
}
