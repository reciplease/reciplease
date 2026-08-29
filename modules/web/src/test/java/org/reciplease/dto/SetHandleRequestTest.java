package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class SetHandleRequestTest {

    @Test
    @DisplayName("a handle within 1-30 characters is accepted")
    void validHandleIsAccepted(final Validator validator) {
        assertThat(validator.validate(new SetHandleRequest("valid-handle")), empty());
    }

    @Test
    @DisplayName("an empty handle is rejected")
    void emptyHandleIsRejected(final Validator validator) {
        assertThat(validator.validate(new SetHandleRequest("")), not(empty()));
    }

    @Test
    @DisplayName("a handle longer than 30 characters is rejected")
    void overlongHandleIsRejected(final Validator validator) {
        assertThat(validator.validate(new SetHandleRequest("a".repeat(31))), not(empty()));
    }
}
