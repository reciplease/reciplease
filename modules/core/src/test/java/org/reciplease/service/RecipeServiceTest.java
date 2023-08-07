package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.IngredientRepository;
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
    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("get recipe by UUID")
    void getRecipe() {
        final var toast = Recipe.builder()
                .id(UUID.randomUUID())
                .name("toast")
                .build();

        when(recipeRepository.findById(toast.getId())).thenReturn(Optional.of(toast));

        final var optionalRecipe = recipeService.findById(toast.getId());

        assertTrue(optionalRecipe.isPresent());
        assertThat(optionalRecipe.get(), is(toast));
    }

    @Test
    @DisplayName("get all recipes")
    void findAll() {
        final var recipe1 = Recipe.builder()
                .id(UUID.randomUUID())
                .name("toast")
                .build();
        final var recipe2 = Recipe.builder()
                .id(UUID.randomUUID())
                .name("soup")
                .build();
        final var recipes = List.of(recipe1, recipe2);

        when(recipeRepository.findAll()).thenReturn(recipes);

        final var actualRecipes = recipeService.findAll();

        assertThat(actualRecipes, is(recipes));
    }

    @Test
    void createRecipe() {
        final var newRecipe = Recipe.builder()
                .name("toast")
                .build();
        final var savedRecipe = newRecipe.toBuilder()
                .id(UUID.randomUUID())
                .build();

        when(recipeRepository.save(newRecipe)).thenReturn(savedRecipe);

        final Recipe actualRecipe = recipeService.create(newRecipe);

        assertThat(actualRecipe, is(savedRecipe));
    }

    @Nested
    class AddRecipeIngredient {
        private Recipe recipe;
        private Ingredient ingredient;

        @BeforeEach
        void setUp() {
            recipe = Recipe.builder()
                    .id(UUID.randomUUID())
                    .name("toast")
                    .build();
            ingredient = Ingredient.builder()
                    .id(UUID.randomUUID())
                    .name("bread")
                    .build();
        }

        @Test
        void shouldAddIngredientToRecipe() {
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
            when(ingredientRepository.findById(ingredient.getId())).thenReturn(Optional.of(ingredient));
            when(recipeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            final var recipeIngredients = recipeService.addIngredient(recipe.getId(), new AddIngredient(ingredient.getId(), 10d));

            assertThat(recipeIngredients, contains(new RecipeIngredient(ingredient, 10d)));
        }

        @Test
        void shouldFail_recipeNotFound() {
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.empty());

            final var exception = assertThrows(IllegalArgumentException.class, () -> recipeService.addIngredient(recipe.getId(), new AddIngredient(ingredient.getId(), 10d)));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }

        @Test
        void shouldFail_ingredientNotFound() {
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
            when(ingredientRepository.findById(ingredient.getId())).thenReturn(Optional.empty());

            final var exception = assertThrows(IllegalArgumentException.class, () -> recipeService.addIngredient(recipe.getId(), new AddIngredient(ingredient.getId(), 10d)));

            assertThat(exception.getMessage(), is("Ingredient does not exist"));
        }
    }
}
