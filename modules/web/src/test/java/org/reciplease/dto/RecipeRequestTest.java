package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.ListMethod;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.TableCell;
import org.reciplease.model.TableMethod;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class RecipeRequestTest {

    @Test
    @DisplayName("converts every writable field to an entity")
    void toEntity() {
        var request = RecipeRequest.builder()
                .name("Pasta")
                .description("Quick")
                .sourceUrl("https://example.com/pasta")
                .isPublic(true)
                .prepTimeMinutes(5)
                .cookTimeMinutes(40)
                .servings(4)
                .method(new ListMethodDto(List.of("Boil")))
                .ingredients(Set.of(new RecipeIngredientDto("pasta", "g", 200d)))
                .build();

        var recipe = request.toEntity();

        assertThat(recipe.name(), is("Pasta"));
        assertThat(recipe.description(), is("Quick"));
        assertThat(recipe.sourceUrl(), is("https://example.com/pasta"));
        assertThat(recipe.isPublic(), is(true));
        assertThat(recipe.prepTimeMinutes(), is(5));
        assertThat(recipe.cookTimeMinutes(), is(40));
        assertThat(recipe.servings(), is(4));
        assertThat(recipe.method(), is(new ListMethod(List.of("Boil"))));
        assertThat(recipe.recipeIngredients(), contains(new RecipeIngredient("pasta", "g", 200d)));
    }

    @Test
    @DisplayName("an entity from a request has no id")
    void toEntityHasNoId() {
        assertThat(validRequest().build().toEntity().id(), is((String) null));
    }

    @Test
    @DisplayName("defaults to private with no times or servings")
    void defaults() {
        var recipe = validRequest().build().toEntity();

        assertThat(recipe.isPublic(), is(false));
        assertThat(recipe.prepTimeMinutes(), is((Integer) null));
        assertThat(recipe.cookTimeMinutes(), is((Integer) null));
        assertThat(recipe.servings(), is((Integer) null));
        assertThat(recipe.recipeIngredients(), is(empty()));
    }

    @Test
    @DisplayName("ingredient measures are normalised to their short form")
    void normalisesIngredientMeasures() {
        var request = validRequest()
                .ingredients(Set.of(new RecipeIngredientDto("bread", "ITEMS", 2d)))
                .build();

        assertThat(request.toEntity().recipeIngredients(), contains(new RecipeIngredient("bread", "item", 2d)));
    }

    @Test
    @DisplayName("name, a method and ingredients are required")
    void nameMethodAndIngredientsAreRequired(final Validator validator) {
        assertThat(validator.validate(validRequest().build()), is(empty()));
        assertThat(validator.validate(validRequest().name(null).build()), not(empty()));
        assertThat(validator.validate(validRequest().method(null).build()), not(empty()));
        assertThat(validator.validate(validRequest().ingredients(null).build()), not(empty()));
    }

    @Test
    @DisplayName("a list method needs its steps")
    void listMethodNeedsSteps(final Validator validator) {
        assertThat(
                validator.validate(
                        validRequest().method(new ListMethodDto(null)).build()),
                not(empty()));
    }

    @Test
    @DisplayName("a table method converts to a table and needs its cells")
    void tableMethodConvertsToTable(final Validator validator) {
        var request = validRequest()
                .method(new TableMethodDto(
                        List.of(new TableCellDto("toast", List.of(new TableCellDto("bread", List.of()))))))
                .build();

        assertThat(validator.validate(request), is(empty()));
        assertThat(
                request.toEntity().method(),
                is(new TableMethod(List.of(new TableCell("toast", List.of(TableCell.row("bread")))))));
        assertThat(
                validator.validate(
                        validRequest().method(new TableMethodDto(null)).build()),
                not(empty()));
    }

    @Test
    @DisplayName("a blank cell anywhere in the tree is rejected")
    void blankNestedCellIsRejected(final Validator validator) {
        var request = validRequest()
                .method(new TableMethodDto(
                        List.of(new TableCellDto("toast", List.of(new TableCellDto(" ", List.of()))))))
                .build();

        assertThat(validator.validate(request), not(empty()));
    }

    @Test
    @DisplayName("description and sourceUrl are optional")
    void descriptionAndSourceUrlAreOptional(final Validator validator) {
        var request = validRequest().build();

        assertThat(validator.validate(request), is(empty()));
        assertThat(request.toEntity().description(), is((String) null));
        assertThat(request.toEntity().sourceUrl(), is((String) null));
    }

    @Test
    @DisplayName("an empty sourceUrl is accepted")
    void emptySourceUrlIsAccepted(final Validator validator) {
        assertThat(validator.validate(validRequest().sourceUrl("").build()), is(empty()));
    }

    @Test
    @DisplayName("an invalid sourceUrl is rejected")
    void invalidSourceUrlIsRejected(final Validator validator) {
        var request = validRequest().sourceUrl("not-a-valid-url").build();

        assertThat(validator.validate(request), not(empty()));
    }

    @Test
    @DisplayName("a name longer than 200 characters is rejected")
    void overlongNameIsRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().name("a".repeat(201)).build()), not(empty()));
    }

    @Test
    @DisplayName("negative times and zero servings are rejected")
    void invalidNumbersAreRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().prepTimeMinutes(-1).build()), not(empty()));
        assertThat(validator.validate(validRequest().cookTimeMinutes(-1).build()), not(empty()));
        assertThat(validator.validate(validRequest().servings(0).build()), not(empty()));
    }

    private static RecipeRequest.RecipeRequestBuilder validRequest() {
        return RecipeRequest.builder()
                .name("Toast")
                .method(new ListMethodDto(List.of()))
                .ingredients(Set.of());
    }
}
