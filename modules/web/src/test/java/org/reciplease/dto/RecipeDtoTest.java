package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.ListMethod;
import org.reciplease.model.Recipe;
import org.reciplease.model.TableCell;
import org.reciplease.model.TableMethod;
import tools.jackson.databind.json.JsonMapper;

class RecipeDtoTest {

    @Test
    @DisplayName("create public DTO from entity")
    void fromEntityPublic() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .description("A staple and classic")
                .method(new ListMethod(List.of("Toast the bread", "Spread butter on toast")))
                .build();

        var recipeDto = RecipeDto.publicView(recipe);

        assertThat(recipeDto.recipeId(), is(recipe.id()));
        assertThat(recipeDto.name(), is(recipe.name()));
        assertThat(recipeDto.description(), is(recipe.description()));
        assertThat(recipeDto.method(), is(new ListMethodDto(List.of("Toast the bread", "Spread butter on toast"))));
        assertThat(recipeDto.ownership(), is((OwnershipDto) null));
    }

    @Test
    @DisplayName("a list recipe serialises its steps under method")
    void listRecipeSerialisesStepsUnderMethod() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .method(new ListMethod(List.of("Toast it")))
                .build();

        var json = JsonMapper.builder().findAndAddModules().build().writeValueAsString(RecipeDto.publicView(recipe));

        assertThat(json, containsString("\"method\":{\"steps\":[\"Toast it\"]}"));
    }

    @Test
    @DisplayName("a table recipe serialises its cells under method")
    void tableRecipeSerialisesCellsUnderMethod() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .method(new TableMethod(List.of(new TableCell("toast", List.of(TableCell.row("bread"))))))
                .build();

        var json = JsonMapper.builder().findAndAddModules().build().writeValueAsString(RecipeDto.publicView(recipe));

        assertThat(
                json,
                containsString(
                        "\"method\":{\"cells\":[{\"text\":\"toast\",\"inputs\":[{\"text\":\"bread\",\"inputs\":[]}]}]}"));
        assertThat(json, not(containsString("\"steps\"")));
    }

    @Test
    @DisplayName("description and steps default to null when not set")
    void fromEntityDefaults() {
        var recipe =
                Recipe.builder().id(UUID.randomUUID().toString()).name("Toast").build();

        var recipeDto = RecipeDto.publicView(recipe);

        assertThat(recipeDto.description(), is((String) null));
    }

    @Test
    @DisplayName("carries the public flag but never owner info")
    void carriesPublicFlagOnly() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .createdBy("user-owner")
                .updatedBy("user-2")
                .isPublic(true)
                .build();

        var recipeDto = RecipeDto.publicView(recipe);

        assertThat(recipeDto.isPublic(), is(true));
        assertThat(recipeDto.ownership(), is((OwnershipDto) null));
    }

    @Test
    @DisplayName("sourceUrl round-trips through DTO")
    void sourceUrlRoundTrips() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Pasta")
                .sourceUrl("https://www.bbcgoodfood.com/recipes/pasta")
                .build();

        var dto = RecipeDto.publicView(recipe);
        assertThat(dto.sourceUrl(), is("https://www.bbcgoodfood.com/recipes/pasta"));
    }

    @Test
    @DisplayName("carries the upvote count")
    void carriesUpvoteCount() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .upvotedBy(Set.of("user-1", "user-2"))
                .build();

        var recipeDto = RecipeDto.publicView(recipe);

        assertThat(recipeDto.upvoteCount(), is(2));
        assertThat(recipeDto.upvotedByCurrentUser(), is(false));
    }

    @Test
    @DisplayName("reflects the viewer's upvote state")
    void reflectsViewerUpvoteState() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .upvotedBy(Set.of("user-1"))
                .build();

        var voted = RecipeDto.publicView(recipe, "user-1");
        var notVoted = RecipeDto.publicView(recipe, "user-2");

        assertThat(voted.upvotedByCurrentUser(), is(true));
        assertThat(notVoted.upvotedByCurrentUser(), is(false));
    }

    @Test
    @DisplayName("sourceUrl is null when not set")
    void sourceUrlNullByDefault() {
        var recipe =
                Recipe.builder().id(UUID.randomUUID().toString()).name("Toast").build();
        assertThat(RecipeDto.publicView(recipe).sourceUrl(), is((String) null));
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

        var dto = RecipeDto.publicView(recipe);
        assertThat(dto.prepTimeMinutes(), is(5));
        assertThat(dto.cookTimeMinutes(), is(40));
        assertThat(dto.servings(), is(4));
    }

    @Test
    @DisplayName("prep time, cook time and servings default to null")
    void timesAndServingsDefaultToNull() {
        var dto = RecipeDto.publicView(Recipe.builder().name("Toast").build());

        assertThat(dto.prepTimeMinutes(), is((Integer) null));
        assertThat(dto.cookTimeMinutes(), is((Integer) null));
        assertThat(dto.servings(), is((Integer) null));
    }

    @Test
    @DisplayName("carries createdBy and owner info in ownership object")
    void carriesOwnerInfo() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .createdBy("user-owner")
                .build();
        var createdBy =
                UserSummaryDto.builder().userId("user-1").handle("alice").build();
        var updatedBy = UserSummaryDto.builder().userId("user-2").handle("bob").build();

        var recipeDto = RecipeDto.ownerView(recipe, createdBy, updatedBy);

        assertThat(recipeDto.ownership().createdBy(), is(createdBy));
        assertThat(recipeDto.ownership().updatedBy(), is(updatedBy));
        assertThat(recipeDto.ownership(), is(not(nullValue())));
    }

    @Test
    @DisplayName("createdBy/updatedBy may be null when the underlying user isn't resolvable")
    void ownerInfoMayBeNull() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .createdBy("user-owner")
                .build();

        var recipeDto = RecipeDto.ownerView(recipe, null, null);

        assertThat(recipeDto.ownership().createdBy(), is((UserSummaryDto) null));
        assertThat(recipeDto.ownership().updatedBy(), is((UserSummaryDto) null));
    }

    @Test
    @DisplayName("carries the upvote count and viewer state for owner view")
    void carriesUpvoteInfo() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .upvotedBy(Set.of("user-1"))
                .build();

        var recipeDto = RecipeDto.ownerView(recipe, null, null, "user-1");

        assertThat(recipeDto.upvoteCount(), is(1));
        assertThat(recipeDto.upvotedByCurrentUser(), is(true));
    }

    @Test
    @DisplayName("public view JSON has no ownership key and no owned key")
    void publicViewJsonShape() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .method(new ListMethod(List.of("Toast it")))
                .build();

        var json = JsonMapper.builder().findAndAddModules().build().writeValueAsString(RecipeDto.publicView(recipe));

        assertThat(json, not(containsString("\"ownership\"")));
        assertThat(json, not(containsString("\"owned\"")));
    }

    @Test
    @DisplayName("owner view JSON has ownership.createdBy.handle")
    void ownerViewJsonShape() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .method(new ListMethod(List.of("Toast it")))
                .build();
        var createdBy =
                UserSummaryDto.builder().userId("user-1").handle("alice").build();
        var updatedBy = UserSummaryDto.builder().userId("user-2").handle("bob").build();

        var json = JsonMapper.builder()
                .findAndAddModules()
                .build()
                .writeValueAsString(RecipeDto.ownerView(recipe, createdBy, updatedBy));

        assertThat(json, containsString("\"ownership\""));
        assertThat(json, containsString("\"createdBy\""));
        assertThat(json, containsString("\"alice\""));
        assertThat(json, not(containsString("\"owned\"")));
    }
}
