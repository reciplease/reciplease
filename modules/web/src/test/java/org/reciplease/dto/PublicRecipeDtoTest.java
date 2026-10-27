package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Recipe;

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
        var recipe =
                Recipe.builder().id(UUID.randomUUID().toString()).name("Toast").build();

        var recipeDto = PublicRecipeDto.from(recipe);

        assertThat(recipeDto.getDescription(), is((String) null));
    }

    @Test
    @DisplayName("carries the public flag but never owner info")
    void carriesPublicFlagOnly() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .createdBy("user-owner")
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
    }

    @Test
    @DisplayName("carries the upvote count")
    void carriesUpvoteCount() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .upvotedBy(Set.of("user-1", "user-2"))
                .build();

        var recipeDto = PublicRecipeDto.from(recipe);

        assertThat(recipeDto.getUpvoteCount(), is(2));
        assertThat(recipeDto.isUpvotedByCurrentUser(), is(false));
    }

    @Test
    @DisplayName("reflects the viewer's upvote state")
    void reflectsViewerUpvoteState() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .upvotedBy(Set.of("user-1"))
                .build();

        var voted = PublicRecipeDto.from(recipe, "user-1");
        var notVoted = PublicRecipeDto.from(recipe, "user-2");

        assertThat(voted.isUpvotedByCurrentUser(), is(true));
        assertThat(notVoted.isUpvotedByCurrentUser(), is(false));
    }

    @Test
    @DisplayName("sourceUrl is null when not set")
    void sourceUrlNullByDefault() {
        var recipe =
                Recipe.builder().id(UUID.randomUUID().toString()).name("Toast").build();
        assertThat(PublicRecipeDto.from(recipe).getSourceUrl(), is((String) null));
    }

    @Test
    @DisplayName("carries prep time, cook time and servings")
    void carriesTimesAndServings() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Pasta")
                .prepTimeMinutes(5)
                .cookTimeMinutes(40)
                .servings(4)
                .build();

        var dto = PublicRecipeDto.from(recipe);
        assertThat(dto.getPrepTimeMinutes(), is(5));
        assertThat(dto.getCookTimeMinutes(), is(40));
        assertThat(dto.getServings(), is(4));
    }

    @Test
    @DisplayName("prep time, cook time and servings default to null")
    void timesAndServingsDefaultToNull() {
        var dto = PublicRecipeDto.from(Recipe.builder().name("Toast").build());

        assertThat(dto.getPrepTimeMinutes(), is((Integer) null));
        assertThat(dto.getCookTimeMinutes(), is((Integer) null));
        assertThat(dto.getServings(), is((Integer) null));
    }
}
