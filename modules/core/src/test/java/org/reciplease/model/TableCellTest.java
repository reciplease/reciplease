package org.reciplease.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TableCellTest {

    @Test
    @DisplayName("a plain row has no duration and is not hands-off")
    void rowDefaults() {
        var cell = TableCell.row("bread");

        assertThat(cell.duration(), is(nullValue()));
        assertThat(cell.handsOff(), is(false));
        assertThat(cell.inputs(), is(List.of()));
    }

    @Test
    @DisplayName("the text and inputs constructor defaults duration and hands-off")
    void twoArgumentConstructorDefaults() {
        var cell = new TableCell("toast", List.of(TableCell.row("bread")));

        assertThat(cell.duration(), is(nullValue()));
        assertThat(cell.handsOff(), is(false));
    }

    @Test
    @DisplayName("carries a duration and the hands-off flag")
    void carriesDurationAndHandsOff() {
        var cell = new TableCell("bake", List.of(), new CellDuration(20, 25), true);

        assertThat(cell.duration(), is(new CellDuration(20, 25)));
        assertThat(cell.handsOff(), is(true));
    }
}
