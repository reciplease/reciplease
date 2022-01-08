package org.reciplease.repository;

import org.junit.jupiter.api.Test;
import org.reciplease.model.PlannedRecipeJpa;
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
public class PlannedRecipeJpaRepositoryTest {
    @Autowired
    private PlannedRecipeRepository plannedRecipeRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    public void shouldReturnPlannedRecipesByDate() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .recipeIngredients(Set.of())
                .build());
        final PlannedRecipeJpa plannedRecipeJpa = plannedRecipeRepository.save(new PlannedRecipeJpa(recipe, LocalDate.of(2019, 2, 2)));

        final List<PlannedRecipeJpa> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipeJpa));
    }

    @Test
    public void shouldReturnEmptyList() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .recipeIngredients(Set.of())
                .build());
        plannedRecipeRepository.save(new PlannedRecipeJpa(recipe, LocalDate.of(2019, 2, 5)));

        final List<PlannedRecipeJpa> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }
}
