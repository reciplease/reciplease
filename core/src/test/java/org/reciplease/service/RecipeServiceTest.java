package org.reciplease.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.Recipe;
import org.reciplease.repository.RecipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@MockitoSettings
class RecipeServiceTest {
    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("get recipe by UUID")
    void getRecipe() {
        final var toast = Recipe.builder()
                .randomUUID()
                .name("toast")
                .build();

        when(recipeRepository.findById(toast.getUuid())).thenReturn(Optional.of(toast));

        final var optionalRecipe = recipeService.findById(toast.getUuid());

        assertTrue(optionalRecipe.isPresent());
        assertThat(optionalRecipe.get(), is(toast));
    }

    @Test
    @DisplayName("UUID does not exist")
    void doesNotExist() {
        final var uuid = UUID.randomUUID();

        when(recipeRepository.findById(uuid)).thenReturn(Optional.empty());

        final var optionalRecipe = recipeService.findById(uuid);

        assertTrue(optionalRecipe.isEmpty());
    }

    @Test
    @DisplayName("get all recipes")
    void findAll() {
        final var recipe1 = Recipe.builder()
                .randomUUID()
                .name("toast")
                .build();
        final var recipe2 = Recipe.builder()
                .randomUUID()
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
                .randomUUID()
                .build();

        when(recipeRepository.save(newRecipe)).thenReturn(savedRecipe);

        final Recipe actualRecipe = recipeService.create(newRecipe);

        assertThat(actualRecipe, is(savedRecipe));
    }
}