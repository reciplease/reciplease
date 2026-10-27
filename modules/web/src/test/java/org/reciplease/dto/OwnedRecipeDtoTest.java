package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Recipe;

class OwnedRecipeDtoTest {

    @Test
    @DisplayName("carries createdBy and owner info")
    void carriesOwnerInfo() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .createdBy("user-owner")
                .build();
        var createdBy =
                UserSummaryDto.builder().userId("user-1").handle("alice").build();
        var updatedBy = UserSummaryDto.builder().userId("user-2").handle("bob").build();

        var recipeDto = OwnedRecipeDto.from(recipe, createdBy, updatedBy);

        assertThat(recipeDto.getCreatedBy(), is(createdBy));
        assertThat(recipeDto.getUpdatedBy(), is(updatedBy));
        assertThat(recipeDto.isOwned(), is(true));
    }

    @Test
    @DisplayName("createdBy/updatedBy may be null when the underlying user isn't resolvable")
    void ownerInfoMayBeNull() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .createdBy("user-owner")
                .build();

        var recipeDto = OwnedRecipeDto.from(recipe, null, null);

        assertThat(recipeDto.getCreatedBy(), is((UserSummaryDto) null));
        assertThat(recipeDto.getUpdatedBy(), is((UserSummaryDto) null));
    }

    @Test
    @DisplayName("carries the upvote count and viewer state")
    void carriesUpvoteInfo() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .upvotedBy(Set.of("user-1"))
                .build();

        var recipeDto = OwnedRecipeDto.from(recipe, null, null, "user-1");

        assertThat(recipeDto.getUpvoteCount(), is(1));
        assertThat(recipeDto.isUpvotedByCurrentUser(), is(true));
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

        var recipeDto = OwnedRecipeDto.from(recipe, null, null);

        assertThat(recipeDto.getPrepTimeMinutes(), is(5));
        assertThat(recipeDto.getCookTimeMinutes(), is(40));
        assertThat(recipeDto.getServings(), is(4));
    }
}
