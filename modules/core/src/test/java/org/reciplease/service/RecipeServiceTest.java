package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.RecipeRepository;
import org.reciplease.service.request.AddIngredient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MockitoSettings
class RecipeServiceTest {
    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("get recipe by id")
    void getRecipe() {
        var toast = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("toast")
                .build();

        when(recipeRepository.findById(toast.id())).thenReturn(Optional.of(toast));

        var optionalRecipe = recipeService.findById(toast.id());

        assertTrue(optionalRecipe.isPresent());
        assertThat(optionalRecipe.get(), is(toast));
    }

    @Test
    @DisplayName("get all recipes")
    void findAll() {
        var recipe1 = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("toast")
                .build();
        var recipe2 = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .build();
        var recipes = List.of(recipe1, recipe2);

        when(recipeRepository.findAll()).thenReturn(recipes);

        var actualRecipes = recipeService.findAll();

        assertThat(actualRecipes, is(recipes));
    }

    @Test
    void createRecipe() {
        var newRecipe = Recipe.builder()
                .name("toast")
                .build();
        var savedRecipe = newRecipe.toBuilder()
                .id(UUID.randomUUID().toString())
                .build();

        when(recipeRepository.save(newRecipe)).thenReturn(savedRecipe);

        var actualRecipe = recipeService.create(newRecipe);

        assertThat(actualRecipe, is(savedRecipe));
    }

    @Nested
    class AddRecipeIngredient {
        private Recipe recipe;

        @BeforeEach
        void setUp() {
            recipe = Recipe.builder()
                    .id(UUID.randomUUID().toString())
                    .name("toast")
                    .build();
        }

        @Test
        void shouldAddIngredientToRecipe() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(recipeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var recipeIngredients = recipeService.addIngredient(recipe.id(),
                    new AddIngredient("bread", "ITEMS", 10d));

            assertThat(recipeIngredients, contains(new RecipeIngredient("bread", "ITEMS", 10d)));
        }

        @Test
        void shouldFail_recipeNotFound() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.empty());

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> recipeService.addIngredient(recipe.id(), new AddIngredient("bread", "ITEMS", 10d)));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }
    }
}
