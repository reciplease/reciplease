package org.reciplease.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.House;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.RecipeRepository;
import org.reciplease.service.request.AddIngredient;

@MockitoSettings
class RecipeServiceTest {
    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private HouseRepository houseRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("get recipe by id")
    void getRecipe() {
        var toast =
                Recipe.builder().id(UUID.randomUUID().toString()).name("toast").build();

        when(recipeRepository.findById(toast.id())).thenReturn(Optional.of(toast));

        var optionalRecipe = recipeService.findById(toast.id());

        assertTrue(optionalRecipe.isPresent());
        assertThat(optionalRecipe.get(), is(toast));
    }

    @Test
    @DisplayName("get all recipes")
    void findAll() {
        var recipe1 =
                Recipe.builder().id(UUID.randomUUID().toString()).name("toast").build();
        var recipe2 =
                Recipe.builder().id(UUID.randomUUID().toString()).name("soup").build();
        var recipes = List.of(recipe1, recipe2);

        when(recipeRepository.findAll()).thenReturn(recipes);

        var actualRecipes = recipeService.findAll();

        assertThat(actualRecipes, is(recipes));
    }

    @Test
    @DisplayName("create stamps the authenticated user as owner")
    void createRecipe() {
        var newRecipe = Recipe.builder().name("toast").build();
        var savedRecipe = newRecipe.toBuilder().id(UUID.randomUUID().toString()).build();

        when(recipeRepository.save(newRecipe.toBuilder().ownerId("user-1").build()))
                .thenReturn(savedRecipe);

        var actualRecipe = recipeService.create("user-1", newRecipe);

        assertThat(actualRecipe, is(savedRecipe));
    }

    @Test
    @DisplayName("delete recipe by id")
    void deleteRecipe() {
        var id = UUID.randomUUID().toString();

        recipeService.deleteById(id);

        verify(recipeRepository).deleteById(id);
    }

    @Nested
    class Visibility {

        @Test
        @DisplayName("anonymous caller sees only public recipes")
        void anonymousSeesPublicOnly() {
            recipeService.findVisibleTo(null);

            verify(recipeRepository).findVisibleTo(Set.of());
        }

        @Test
        @DisplayName("viewer can see their own plus housemates' recipes")
        void resolvesHousematesAsVisibleOwners() {
            var house = new House("house-1", "Home", Instant.now());
            when(houseRepository.findAllForUser("viewer")).thenReturn(List.of(house));
            when(houseRepository.members("house-1")).thenReturn(List.of(
                    new HouseMembership("viewer", null, HouseRole.OWNER),
                    new HouseMembership("alice", null, HouseRole.OWNER),
                    new HouseMembership("bob", null, HouseRole.READ_ONLY)));

            recipeService.findVisibleTo("viewer");

            verify(recipeRepository).findVisibleTo(Set.of("viewer", "alice", "bob"));
        }

        @Test
        @DisplayName("distinct owners across all of the viewer's houses")
        void resolvesAcrossMultipleHouses() {
            when(houseRepository.findAllForUser("viewer")).thenReturn(List.of(
                    new House("house-1", "Home", Instant.now()),
                    new House("house-2", "Cottage", Instant.now())));
            when(houseRepository.members("house-1")).thenReturn(List.of(
                    new HouseMembership("viewer", null, HouseRole.OWNER),
                    new HouseMembership("alice", null, HouseRole.READ_ONLY)));
            when(houseRepository.members("house-2")).thenReturn(List.of(
                    new HouseMembership("viewer", null, HouseRole.OWNER),
                    new HouseMembership("carol", null, HouseRole.OWNER)));

            recipeService.findVisibleTo("viewer");

            verify(recipeRepository).findVisibleTo(Set.of("viewer", "alice", "carol"));
        }

        @Test
        @DisplayName("findVisibleById resolves visible owners the same way")
        void findByIdUsesVisibleOwners() {
            when(houseRepository.findAllForUser("viewer")).thenReturn(List.of());
            var recipe = Recipe.builder().id("r1").name("toast").ownerId("viewer").build();
            when(recipeRepository.findVisibleById("r1", Set.of("viewer"))).thenReturn(Optional.of(recipe));

            var found = recipeService.findVisibleById("r1", "viewer");

            assertThat(found, is(Optional.of(recipe)));
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("merges name, description, steps, ingredients and isPublic, preserving owner")
        void mergesUpdatesIntoExisting() {
            var existing = Recipe.builder()
                    .id(UUID.randomUUID().toString())
                    .name("toast")
                    .description("Old description")
                    .ownerId("owner")
                    .createdBy("owner")
                    .build()
                    .addIngredient("bread", "ITEMS", 1d);

            var updates = Recipe.builder()
                    .name("Fancy toast")
                    .description("New description")
                    .steps(List.of("Toast it"))
                    .isPublic(true)
                    .build()
                    .addIngredient("bread", "ITEMS", 2d);

            when(recipeRepository.findById(existing.id())).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var result = recipeService.update(existing.id(), updates);

            assertThat(result.id(), is(existing.id()));
            assertThat(result.ownerId(), is("owner"));
            assertThat(result.createdBy(), is("owner"));
            assertThat(result.name(), is("Fancy toast"));
            assertThat(result.description(), is("New description"));
            assertThat(result.steps(), is(List.of("Toast it")));
            assertThat(result.isPublic(), is(true));
            assertThat(result.recipeIngredients(), contains(new RecipeIngredient("bread", "ITEMS", 2d)));
        }

        @Test
        void shouldFail_recipeNotFound() {
            var id = UUID.randomUUID().toString();
            when(recipeRepository.findById(id)).thenReturn(Optional.empty());

            var exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> recipeService.update(
                            id, Recipe.builder().name("toast").build()));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }
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

            var recipeIngredients = recipeService.addIngredient(recipe.id(), new AddIngredient("bread", "ITEMS", 10d));

            assertThat(recipeIngredients, contains(new RecipeIngredient("bread", "ITEMS", 10d)));
        }

        @Test
        void shouldFail_recipeNotFound() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.empty());

            var exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> recipeService.addIngredient(recipe.id(), new AddIngredient("bread", "ITEMS", 10d)));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }
    }
}
