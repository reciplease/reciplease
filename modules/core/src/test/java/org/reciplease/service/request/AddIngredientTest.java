package org.reciplease.service.request;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class AddIngredientTest {

    @Test
    @DisplayName("a valid ingredient has no constraint violations")
    void valid(final Validator validator) {
        final var violations = validator.validate(new AddIngredient("tomato", "item", 10d));

        assertThat(violations, empty());
    }

    @Test
    @DisplayName("a blank name is rejected")
    void blankName(final Validator validator) {
        final var violations = validator.validate(new AddIngredient(" ", "item", 10d));

        assertThat(violations, hasSize(1));
    }

    @Test
    @DisplayName("a blank measure is rejected")
    void blankMeasure(final Validator validator) {
        final var violations = validator.validate(new AddIngredient("tomato", " ", 10d));

        assertThat(violations, hasSize(1));
    }

    @Test
    @DisplayName("a zero amount is rejected")
    void zeroAmount(final Validator validator) {
        final var violations = validator.validate(new AddIngredient("tomato", "item", 0d));

        assertThat(violations, not(empty()));
    }

    @Test
    @DisplayName("a negative amount is rejected")
    void negativeAmount(final Validator validator) {
        final var violations = validator.validate(new AddIngredient("tomato", "item", -1d));

        assertThat(violations, not(empty()));
    }
}
