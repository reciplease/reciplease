package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import jakarta.validation.Validator;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class PantryItemRequestTest {

    @Test
    @DisplayName("create entity from request")
    void toEntity() {
        var itemRequest = PantryItemRequest.builder()
                .name("bread")
                .brand("Warburtons")
                .measure("g")
                .amount(10d)
                .remaining(6d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .image(new byte[] {1, 2, 3})
                .build();

        var item = itemRequest.toEntity("house-1");

        assertThat(item.id(), is(nullValue()));
        assertThat(item.houseId(), is("house-1"));
        assertThat(item.name(), is(itemRequest.getName()));
        assertThat(item.brand(), is(itemRequest.getBrand()));
        // A short id passes through unchanged.
        assertThat(item.measure(), is("g"));
        assertThat(item.amount(), is(itemRequest.getAmount()));
        assertThat(item.remaining(), is(itemRequest.getRemaining()));
        assertThat(item.expiration(), is(itemRequest.getExpiration()));
        assertThat(item.barcode(), is(itemRequest.getBarcode()));
        assertThat(item.image(), is(equalTo(itemRequest.getImage())));
    }

    @Test
    @DisplayName("a valid request has no constraint violations")
    void validRequestHasNoViolations(final Validator validator) {
        assertThat(validator.validate(validRequest().build()), is(empty()));
    }

    @Test
    @DisplayName("name, measure, amount and expiration are required")
    void requiredFieldsAreRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().name(null).build()), not(empty()));
        assertThat(validator.validate(validRequest().measure(null).build()), not(empty()));
        assertThat(validator.validate(validRequest().amount(null).build()), not(empty()));
        assertThat(validator.validate(validRequest().expiration(null).build()), not(empty()));
    }

    @Test
    @DisplayName("remaining is optional and defaults to the amount")
    void remainingIsOptional(final Validator validator) {
        assertThat(validator.validate(validRequest().remaining(null).build()), is(empty()));
        assertThat(validRequest().remaining(null).build().toEntity("house-1").remaining(), is(10d));
    }

    @Test
    @DisplayName("a zero amount is rejected")
    void zeroAmountIsRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().amount(0d).build()), not(empty()));
    }

    @Test
    @DisplayName("a negative remaining is rejected")
    void negativeRemainingIsRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().remaining(-1d).build()), not(empty()));
    }

    @Test
    @DisplayName("a name longer than 200 characters is rejected")
    void overlongNameIsRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().name("a".repeat(201)).build()), not(empty()));
    }

    @Test
    @DisplayName("a brand longer than 200 characters is rejected")
    void overlongBrandIsRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().brand("a".repeat(201)).build()), not(empty()));
    }

    @Test
    @DisplayName("an invalid barcode is rejected")
    void invalidBarcodeIsRejected(final Validator validator) {
        assertThat(validator.validate(validRequest().barcode("abc123").build()), not(empty()));
    }

    @Test
    @DisplayName("an empty barcode is accepted")
    void emptyBarcodeIsAccepted(final Validator validator) {
        assertThat(validator.validate(validRequest().barcode("").build()), is(empty()));
    }

    private PantryItemRequest.PantryItemRequestBuilder validRequest() {
        return PantryItemRequest.builder()
                .name("bread")
                .measure("item")
                .amount(10d)
                .remaining(10d)
                .expiration(LocalDate.now());
    }
}
