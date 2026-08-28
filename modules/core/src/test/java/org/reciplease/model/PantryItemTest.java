package org.reciplease.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PantryItemTest {
    @Test
    @DisplayName("remaining defaults to amount when not supplied")
    void remainingDefaultsToAmount() {
        var item = new PantryItem(null, null, "house-1", "bread", null, "ITEMS", 10d, LocalDate.now(), null);

        assertThat(item.remaining(), is(10d));
    }

    @Test
    @DisplayName("remaining is kept when explicitly supplied")
    void remainingIsKeptWhenSupplied() {
        var item = new PantryItem(null, null, "house-1", "bread", null, "ITEMS", 10d, 4d, LocalDate.now(), null);

        assertThat(item.remaining(), is(4d));
    }

    @Test
    @DisplayName("withRemaining replaces only the remaining amount")
    void withRemaining() {
        var item = new PantryItem(null, null, "house-1", "bread", null, "ITEMS", 10d, LocalDate.now(), null);

        var updated = item.withRemaining(3d);

        assertThat(updated.remaining(), is(3d));
        assertThat(updated.amount(), is(item.amount()));
    }

    @Test
    @DisplayName("withId preserves brand")
    void withIdPreservesBrand() {
        var item = new PantryItem(null, null, "house-1", "tomato ketchup", "Heinz", "ITEMS", 10d, LocalDate.now(), null);

        var updated = item.withId("new-id");

        assertThat(updated.brand(), is("Heinz"));
    }
}
