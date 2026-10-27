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
class PendingPantryItemRequestTest {

    @Test
    @DisplayName("create entity from request without taking an id or barcode from the body")
    void toEntity() {
        var itemRequest = PendingPantryItemRequest.builder()
                .scanId("scan-1")
                .barcodeImage(new byte[] {1})
                .expirationImage(new byte[] {2})
                .measureImage(new byte[] {3})
                .build();

        var item = itemRequest.toEntity("house-1");

        assertThat(item.id(), is(nullValue()));
        assertThat(item.houseId(), is("house-1"));
        assertThat(item.scanId(), is("scan-1"));
        assertThat(item.barcodeImage(), is(itemRequest.getBarcodeImage()));
        assertThat(item.expirationImage(), is(itemRequest.getExpirationImage()));
        assertThat(item.measureImage(), is(itemRequest.getMeasureImage()));
        assertThat(item.legacyBarcode(), is(nullValue()));
    }

    @Test
    @DisplayName("a scan id longer than 64 characters is rejected")
    void overlongScanIdIsRejected(final Validator validator) {
        var itemRequest =
                PendingPantryItemRequest.builder().scanId("a".repeat(65)).build();

        assertThat(validator.validate(itemRequest), is(not(empty())));
    }
}
