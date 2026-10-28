package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.CellDuration;
import org.reciplease.model.TableCell;
import org.reciplease.validation.ValidationTest;
import tools.jackson.databind.json.JsonMapper;

@ValidationTest
class TableCellDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    @DisplayName("a cell without duration or handsOff deserialises to no duration and not hands-off")
    void absentFieldsDefault() {
        var cell = mapper.readValue(
                "{\"text\":\"toast\",\"inputs\":[{\"text\":\"bread\",\"inputs\":[]}]}", TableCellDto.class);

        assertThat(cell.duration(), is(nullValue()));
        assertThat(cell.handsOff(), is(false));
        assertThat(cell.inputs().getFirst().duration(), is(nullValue()));
        assertThat(cell.inputs().getFirst().handsOff(), is(false));
    }

    @Test
    @DisplayName("a cell with duration and handsOff deserialises, nested inputs included")
    void presentFieldsDeserialise() {
        var json = """
                {"text":"roast","inputs":[
                  {"text":"rest","inputs":[],"duration":{"minMinutes":10,"maxMinutes":10},"handsOff":true}],
                 "duration":{"minMinutes":45,"maxMinutes":60},"handsOff":true}
                """;

        var cell = mapper.readValue(json, TableCellDto.class);

        assertThat(cell.duration(), is(new CellDurationDto(45, 60)));
        assertThat(cell.handsOff(), is(true));
        assertThat(cell.inputs().getFirst().duration(), is(new CellDurationDto(10, 10)));
        assertThat(cell.inputs().getFirst().handsOff(), is(true));
    }

    @Test
    @DisplayName("converts to and from the model with duration and handsOff")
    void modelRoundTrip() {
        var model = new TableCell(
                "roast",
                List.of(new TableCell("rest", List.of(), new CellDuration(10, 10), true)),
                new CellDuration(45, 60),
                true);

        var dto = TableCellDto.from(model);

        assertThat(dto.duration(), is(new CellDurationDto(45, 60)));
        assertThat(dto.handsOff(), is(true));
        assertThat(dto.toModel(), is(model));
    }

    @Test
    @DisplayName("a response omits duration when unset but always carries handsOff")
    void responseJsonShape() {
        var plain = mapper.writeValueAsString(TableCellDto.from(TableCell.row("bread")));
        var timed = mapper.writeValueAsString(
                TableCellDto.from(new TableCell("bake", List.of(), new CellDuration(20, 25), true)));

        assertThat(plain, is("{\"text\":\"bread\",\"inputs\":[],\"handsOff\":false}"));
        assertThat(
                timed,
                is(
                        "{\"text\":\"bake\",\"inputs\":[],\"duration\":{\"minMinutes\":20,\"maxMinutes\":25},\"handsOff\":true}"));
        assertThat(plain, not(containsString("duration")));
    }

    @Test
    @DisplayName("a valid range and a single time pass validation")
    void validDurations(final Validator validator) {
        assertThat(validator.validate(new CellDurationDto(5, 15)), is(empty()));
        assertThat(validator.validate(new CellDurationDto(10, 10)), is(empty()));
        assertThat(validator.validate(new CellDurationDto(0, 0)), is(empty()));
    }

    @Test
    @DisplayName("a max below the min is rejected")
    void maxBelowMinRejected(final Validator validator) {
        assertThat(validator.validate(new CellDurationDto(15, 5)), not(empty()));
    }

    @Test
    @DisplayName("a negative min is rejected")
    void negativeRejected(final Validator validator) {
        assertThat(validator.validate(new CellDurationDto(-1, 5)), not(empty()));
    }

    @Test
    @DisplayName("both bounds are required when a duration is present")
    void boundsRequired(final Validator validator) {
        assertThat(validator.validate(new CellDurationDto(null, 5)), not(empty()));
        assertThat(validator.validate(new CellDurationDto(5, null)), not(empty()));
    }

    @Test
    @DisplayName("an invalid duration on a nested input is rejected")
    void nestedInvalidDurationRejected(final Validator validator) {
        var nested = new TableCellDto("rest", List.of(), new CellDurationDto(9, 1), false);
        var cell = new TableCellDto("roast", List.of(nested), null, false);

        assertThat(validator.validate(cell), not(empty()));
    }
}
