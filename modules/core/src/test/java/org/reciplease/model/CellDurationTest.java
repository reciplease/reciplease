package org.reciplease.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CellDurationTest {

    @Test
    @DisplayName("a single time has equal min and max")
    void singleTime() {
        var duration = new CellDuration(10, 10);

        assertThat(duration.minMinutes(), is(10));
        assertThat(duration.maxMinutes(), is(10));
    }

    @Test
    @DisplayName("a range keeps its bounds")
    void range() {
        var duration = new CellDuration(5, 15);

        assertThat(duration.minMinutes(), is(5));
        assertThat(duration.maxMinutes(), is(15));
    }

    @Test
    @DisplayName("zero minutes is allowed")
    void zeroAllowed() {
        assertThat(new CellDuration(0, 0).maxMinutes(), is(0));
    }

    @Test
    @DisplayName("a negative min is rejected")
    void negativeMinRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CellDuration(-1, 5));
    }

    @Test
    @DisplayName("a max below the min is rejected")
    void maxBelowMinRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CellDuration(10, 5));
    }
}
