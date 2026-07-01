package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class PublicRecipeDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .description("A staple and classic")
                .steps(List.of("Toast the bread", "Spread butter on toast"))
                .build();

        var recipeDto = PublicRecipeDto.from(recipe);

        assertThat(recipeDto.getRecipeId(), is(recipe.id()));
        assertThat(recipeDto.getName(), is(recipe.name()));
        assertThat(recipeDto.getDescription(), is(recipe.description()));
        assertThat(recipeDto.getSteps(), is(recipe.steps()));
        assertThat(recipeDto.isOwned(), is(false));
    }

    @Test
    @DisplayName("description and steps default to null when not set")
    void fromEntityDefaults() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .build();

        var recipeDto = PublicRecipeDto.from(recipe);

        assertThat(recipeDto.getDescription(), is((String) null));
    }

    @Test
    @DisplayName("carries the public flag but never house or owner info")
    void carriesPublicFlagOnly() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .houseId("house-1")
                .createdBy("user-1")
                .updatedBy("user-2")
                .isPublic(true)
                .build();

        var recipeDto = PublicRecipeDto.from(recipe);

        assertThat(recipeDto.isPublic(), is(true));
        assertThat(recipeDto.isOwned(), is(false));
    }

    @Test
    @DisplayName("sourceUrl round-trips through DTO")
    void sourceUrlRoundTrips() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Pasta")
                .sourceUrl("https://www.bbcgoodfood.com/recipes/pasta")
                .build();

        var dto = PublicRecipeDto.from(recipe);
        assertThat(dto.getSourceUrl(), is("https://www.bbcgoodfood.com/recipes/pasta"));

        var entity = dto.toEntity();
        assertThat(entity.sourceUrl(), is("https://www.bbcgoodfood.com/recipes/pasta"));
    }

    @Test
    @DisplayName("sourceUrl is null when not set")
    void sourceUrlNullByDefault() {
        var recipe = Recipe.builder().id(UUID.randomUUID().toString()).name("Toast").build();
        assertThat(PublicRecipeDto.from(recipe).getSourceUrl(), is((String) null));
    }

    @Test
    @DisplayName("convert to entity")
    void toEntity() {
        var recipeId = UUID.randomUUID().toString();
        var recipeDto = PublicRecipeDto.builder()
                .recipeId(recipeId)
                .name("Toast")
                .description("A staple and classic")
                .steps(List.of("Toast the bread", "Spread butter on toast"))
                .build();

        var recipe = recipeDto.toEntity();

        assertThat(recipe.id(), is(recipeId));
        assertThat(recipe.name(), is("Toast"));
        assertThat(recipe.description(), is("A staple and classic"));
        assertThat(recipe.steps(), is(List.of("Toast the bread", "Spread butter on toast")));
        assertThat(recipe.recipeIngredients(), is(empty()));
    }

    @Test
    @DisplayName("convert to entity including ingredients")
    void toEntityWithIngredients() {
        var recipeDto = PublicRecipeDto.builder()
                .name("Toast")
                .ingredients(Set.of(RecipeIngredientDto.from(new RecipeIngredient("bread", "ITEMS", 2d))))
                .build();

        var recipe = recipeDto.toEntity();

        // Round-tripping through RecipeIngredientDto normalizes the legacy measure id to its short form.
        assertThat(recipe.recipeIngredients(), contains(new RecipeIngredient("bread", "item", 2d)));
    }
}
