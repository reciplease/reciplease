package org.reciplease.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PlannedRecipeRepositoryTest {
    @Autowired
    private PlannedRecipeRepository plannedRecipeRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnPlannedRecipesByDate() {
        final Recipe recipe = recipeRepository.save(new Recipe(Collections.emptyList()));
        final PlannedRecipe plannedRecipe = plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 2)));

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, contains(plannedRecipe));
    }

    @Test
    public void shouldReturnEmptyList() {
        final Recipe recipe = recipeRepository.save(new Recipe(Collections.emptyList()));
        plannedRecipeRepository.save(new PlannedRecipe(recipe, LocalDate.of(2019, 2, 5)));

        final List<PlannedRecipe> plannedRecipes = plannedRecipeRepository.findByDateIsBetween(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 3));

        assertThat(plannedRecipes, is(empty()));
    }
}