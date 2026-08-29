package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.HouseRole;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class CreateApiKeyRequestTest {

    @Test
    @DisplayName("a valid request has no constraint violations")
    void validRequestHasNoViolations(final Validator validator) {
        var request = new CreateApiKeyRequest("Home Assistant", HouseRole.READ_ONLY);

        assertThat(validator.validate(request), empty());
    }

    @Test
    @DisplayName("a blank name is rejected")
    void blankNameIsRejected(final Validator validator) {
        var request = new CreateApiKeyRequest(" ", HouseRole.READ_ONLY);

        assertThat(validator.validate(request), not(empty()));
    }

    @Test
    @DisplayName("a name longer than 200 characters is rejected")
    void overlongNameIsRejected(final Validator validator) {
        var request = new CreateApiKeyRequest("a".repeat(201), HouseRole.READ_ONLY);

        assertThat(validator.validate(request), not(empty()));
    }

    @Test
    @DisplayName("a null role is rejected")
    void nullRoleIsRejected(final Validator validator) {
        var request = new CreateApiKeyRequest("Home Assistant", null);

        assertThat(validator.validate(request), not(empty()));
    }
}
