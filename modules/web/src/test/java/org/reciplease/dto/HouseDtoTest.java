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
class HouseDtoTest {

    @Test
    @DisplayName("a name longer than 200 characters is rejected")
    void overlongNameIsRejected(final Validator validator) {
        var dto = HouseDto.builder()
                .id("house-1")
                .name("a".repeat(201))
                .role(HouseRole.OWNER)
                .build();

        assertThat(validator.validate(dto), not(empty()));
    }

    @Test
    @DisplayName("a name of exactly 200 characters is accepted")
    void exactlyTwoHundredCharacterNameIsAccepted(final Validator validator) {
        var dto = HouseDto.builder()
                .id("house-1")
                .name("a".repeat(200))
                .role(HouseRole.OWNER)
                .build();

        assertThat(validator.validate(dto), empty());
    }
}
