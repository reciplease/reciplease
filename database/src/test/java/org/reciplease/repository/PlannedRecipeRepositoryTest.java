package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class PlannedRecipeRepositoryTest {
    @Autowired
    private PlannedRecipeRepository plannedRecipeRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnPlannedRecipesByDate() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .recipeIngredients(Set.of())
                .build());
        final PlannedRecipe plannedRecipe = plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 2)));

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipe));
    }

    @Test
    public void shouldReturnEmptyList() {
        final Recipe recipe = recipeRepository.save(Recipe.builder()
                .recipeIngredients(Set.of())
                .build());
        plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 5)));

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }
}