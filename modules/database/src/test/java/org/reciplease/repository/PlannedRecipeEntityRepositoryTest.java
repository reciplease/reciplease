package org.reciplease.repository;

import org.junit.jupiter.api.Test;
import org.reciplease.model.PlannedRecipeEntity;
import org.reciplease.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@DataJpaTest
public class PlannedRecipeEntityRepositoryTest {
    @Autowired
    private PlannedRecipeRepository plannedRecipeRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    public void shouldReturnPlannedRecipesByDate() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .recipeIngredients(Set.of())
                .build());
        final PlannedRecipeEntity plannedRecipeEntity = plannedRecipeRepository.save(new PlannedRecipeEntity(recipe, LocalDate.of(2019, 2, 2)));

        final List<PlannedRecipeEntity> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipeEntity));
    }

    @Test
    public void shouldReturnEmptyList() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .recipeIngredients(Set.of())
                .build());
        plannedRecipeRepository.save(new PlannedRecipeEntity(recipe, LocalDate.of(2019, 2, 5)));

        final List<PlannedRecipeEntity> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }
}
