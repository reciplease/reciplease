package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class PantryAllocationDtoTest {

    @Test
    @DisplayName("a zero amount is accepted")
    void zeroAmountIsAccepted(final Validator validator) {
        var dto =
                PantryAllocationDto.builder().pantryItemId("item-1").amount(0d).build();

        assertThat(validator.validate(dto), empty());
    }

    @Test
    @DisplayName("a negative amount is rejected")
    void negativeAmountIsRejected(final Validator validator) {
        var dto =
                PantryAllocationDto.builder().pantryItemId("item-1").amount(-1d).build();

        assertThat(validator.validate(dto), not(empty()));
    }

    @Test
    @DisplayName("the barcode is snapshotted server-side, never taken from the request")
    void toModelIgnoresBarcode() {
        var dto = PantryAllocationDto.builder()
                .pantryItemId("item-1")
                .barcode("0123456789012")
                .amount(2d)
                .build();

        assertThat(dto.toModel().barcode(), is(nullValue()));
        assertThat(dto.toModel().pantryItemId(), is("item-1"));
        assertThat(dto.toModel().amount(), is(2d));
    }
}
