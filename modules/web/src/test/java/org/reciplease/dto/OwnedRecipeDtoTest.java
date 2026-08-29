package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Recipe;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class OwnedRecipeDtoTest {

    @Test
    @DisplayName("carries houseId and owner info")
    void carriesHouseAndOwnerInfo() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .houseId("house-1")
                .build();
        var createdBy =
                UserSummaryDto.builder().userId("user-1").handle("alice").build();
        var updatedBy = UserSummaryDto.builder().userId("user-2").handle("bob").build();

        var recipeDto = OwnedRecipeDto.from(recipe, createdBy, updatedBy);

        assertThat(recipeDto.getHouseId(), is("house-1"));
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
                .houseId("house-1")
                .build();

        var recipeDto = OwnedRecipeDto.from(recipe, null, null);

        assertThat(recipeDto.getCreatedBy(), is((UserSummaryDto) null));
        assertThat(recipeDto.getUpdatedBy(), is((UserSummaryDto) null));
    }

    @Test
    @DisplayName("an invalid sourceUrl is rejected")
    void invalidSourceUrlIsRejected(final Validator validator) {
        var recipeDto = OwnedRecipeDto.builder()
                .name("Toast")
                .sourceUrl("not-a-valid-url")
                .build();

        assertThat(validator.validate(recipeDto), not(empty()));
    }

    @Test
    @DisplayName("a name longer than 200 characters is rejected")
    void overlongNameIsRejected(final Validator validator) {
        var recipeDto = OwnedRecipeDto.builder().name("a".repeat(201)).build();

        assertThat(validator.validate(recipeDto), not(empty()));
    }
}
