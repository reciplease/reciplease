package org.reciplease.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeDocumentTest {

    @Test
    @DisplayName("prep time, cook time and servings round-trip through the document")
    void timesAndServingsRoundTrip() {
        var recipe = Recipe.builder()
                .id("recipe-1")
                .name("pasta")
                .prepTimeMinutes(5)
                .cookTimeMinutes(40)
                .servings(4)
                .build();

        var document = RecipeDocument.from(recipe);

        assertThat(document.getPrepTimeMinutes(), is(5));
        assertThat(document.getCookTimeMinutes(), is(40));
        assertThat(document.getServings(), is(4));

        var roundTripped = document.toModel();

        assertThat(roundTripped.prepTimeMinutes(), is(5));
        assertThat(roundTripped.cookTimeMinutes(), is(40));
        assertThat(roundTripped.servings(), is(4));
    }

    @Test
    @DisplayName("a document without times or servings maps to null")
    void missingTimesAndServingsAreNull() {
        var document = RecipeDocument.builder().id("recipe-1").name("toast").build();

        var recipe = document.toModel();

        assertThat(recipe.prepTimeMinutes(), is((Integer) null));
        assertThat(recipe.cookTimeMinutes(), is((Integer) null));
        assertThat(recipe.servings(), is((Integer) null));
    }

    @Test
    @DisplayName("create document from entity")
    void fromModel() {
        var recipe = Recipe.builder()
                .id("recipe-1")
                .name("toast")
                .description("A staple")
                .method(new ListMethod(List.of("Toast it")))
                .createdBy("user-1")
                .updatedBy("user-2")
                .build()
                .addIngredient("bread", "ITEMS", 2d);

        var document = RecipeDocument.from(recipe);

        assertThat(document.getId(), is("recipe-1"));
        assertThat(document.getName(), is("toast"));
        assertThat(document.getDescription(), is("A staple"));
        assertThat(document.getSteps(), is(List.of("Toast it")));
        assertThat(document.getCells(), is(nullValue()));
        assertThat(document.getIngredients().getFirst().toModel(), is(new RecipeIngredient("bread", "ITEMS", 2d)));
        assertThat(document.getCreatedBy(), is("user-1"));
        assertThat(document.getUpdatedBy(), is("user-2"));
    }

    @Test
    @DisplayName("creates document with empty steps by default")
    void fromModelWithDefaultMethod() {
        var recipe = Recipe.builder().id("recipe-1").name("toast").build();

        var document = RecipeDocument.from(recipe);

        assertThat(document.getSteps(), is(empty()));
    }

    @Test
    @DisplayName("a table method is stored as cells and round-trips without steps")
    void tableMethodRoundTrips() {
        var table = new TableMethod(List.of(new TableCell(
                "spread", List.of(new TableCell("toast", List.of(TableCell.row("bread"))), TableCell.row("butter")))));
        var recipe = Recipe.builder().id("recipe-1").name("toast").method(table).build();

        var document = RecipeDocument.from(recipe);

        assertThat(document.getSteps(), is(nullValue()));
        assertThat(document.toModel().method(), is(table));
    }

    @Test
    @DisplayName("cell duration and hands-off round-trip, including on nested inputs")
    void cellDurationAndHandsOffRoundTrip() {
        var table = new TableMethod(List.of(new TableCell(
                "roast",
                List.of(
                        new TableCell("rest", List.of(), new CellDuration(10, 10), true),
                        new TableCell("prep", List.of(), null, false)),
                new CellDuration(45, 60),
                true)));
        var recipe = Recipe.builder().id("recipe-1").name("roast").method(table).build();

        var document = RecipeDocument.from(recipe);

        assertThat(document.getCells().getFirst().duration(), is(new CellDurationDocument(45, 60)));
        assertThat(document.getCells().getFirst().handsOff(), is(true));
        assertThat(document.toModel().method(), is(table));
    }

    @Test
    @DisplayName("a stored cell without duration or hands-off loads as no duration and not hands-off")
    void legacyCellLoadsWithDefaults() {
        var legacy =
                new TableCellDocument("toast", List.of(new TableCellDocument("bread", null, null, null)), null, null);
        var document = RecipeDocument.builder()
                .id("recipe-1")
                .name("toast")
                .cells(List.of(legacy))
                .build();

        var cell = ((TableMethod) document.toModel().method()).cells().getFirst();

        assertThat(cell.duration(), is(nullValue()));
        assertThat(cell.handsOff(), is(false));
        assertThat(cell.inputs().getFirst().duration(), is(nullValue()));
        assertThat(cell.inputs().getFirst().handsOff(), is(false));
    }

    @Test
    @DisplayName("cells win over steps when a document carries both")
    void cellsWinOverSteps() {
        var document = RecipeDocument.builder()
                .id("recipe-1")
                .name("toast")
                .steps(List.of("Toast it"))
                .cells(List.of(TableCellDocument.from(TableCell.row("bread"))))
                .build();

        assertThat(document.toModel().method(), is(new TableMethod(List.of(TableCell.row("bread")))));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var document = RecipeDocument.builder()
                .id("recipe-1")
                .name("toast")
                .description("A staple")
                .steps(List.of("Toast it"))
                .ingredients(List.of(RecipeIngredientDocument.from(new RecipeIngredient("bread", "ITEMS", 2d))))
                .createdBy("user-1")
                .updatedBy("user-2")
                .build();

        var recipe = document.toModel();

        assertThat(recipe.id(), is("recipe-1"));
        assertThat(recipe.name(), is("toast"));
        assertThat(recipe.description(), is("A staple"));
        assertThat(recipe.method(), is(new ListMethod(List.of("Toast it"))));
        assertThat(recipe.recipeIngredients(), contains(new RecipeIngredient("bread", "ITEMS", 2d)));
        assertThat(recipe.createdBy(), is("user-1"));
        assertThat(recipe.updatedBy(), is("user-2"));
    }

    @Test
    @DisplayName("round-trips to model when steps and ingredients are null")
    void toModelWithNullStepsAndIngredients() {
        var document = RecipeDocument.builder()
                .id("recipe-1")
                .name("toast")
                .steps(null)
                .ingredients(null)
                .build();

        var recipe = document.toModel();

        assertThat(recipe.method(), is(new ListMethod(List.of())));
        assertThat(recipe.recipeIngredients(), is(empty()));
    }
}
