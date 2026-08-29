package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import jakarta.validation.Validator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.PantryItem;
import org.reciplease.validation.ValidationTest;

@ValidationTest
class PantryItemDtoTest {
    @Test
    @DisplayName("create DTO from entity")
    void from() {
        var item = new PantryItem(
                UUID.randomUUID().toString(),
                null,
                "house-1",
                "bread",
                "Warburtons",
                "ITEMS",
                10d,
                12d,
                LocalDate.now(),
                "0123456789012",
                new byte[] {1, 2, 3},
                Instant.now(),
                Instant.now());

        var itemDto = PantryItemDto.from(item);

        assertThat(itemDto.getUuid(), is(item.id()));
        assertThat(itemDto.getHouseId(), is(item.houseId()));
        assertThat(itemDto.getName(), is(item.name()));
        assertThat(itemDto.getBrand(), is(item.brand()));
        // Legacy measure ids are normalized to their short form.
        assertThat(itemDto.getMeasure(), is("item"));
        assertThat(itemDto.getAmount(), is(item.amount()));
        assertThat(itemDto.getRemaining(), is(item.remaining()));
        assertThat(itemDto.getExpiration(), is(item.expiration()));
        assertThat(itemDto.getBarcode(), is(item.barcode()));
        assertThat(itemDto.getImage(), is(equalTo(item.image())));
        assertThat(itemDto.getCreatedAt(), is(item.createdAt()));
        assertThat(itemDto.getUpdatedAt(), is(item.updatedAt()));
    }

    @Test
    @DisplayName("create DTO from entity with no image")
    void fromWithNoImage() {
        var item = new PantryItem(
                UUID.randomUUID().toString(),
                null,
                "house-1",
                "bread",
                null,
                "ITEMS",
                10d,
                LocalDate.now(),
                "0123456789012");

        var itemDto = PantryItemDto.from(item);

        assertThat(itemDto.getImage(), is(nullValue()));
    }

    @Test
    @DisplayName("create entity from DTO")
    void toEntity() {
        var itemDto = PantryItemDto.builder()
                .uuid(UUID.randomUUID().toString())
                .name("bread")
                .brand("Warburtons")
                .measure("g")
                .amount(10d)
                .remaining(6d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .image(new byte[] {1, 2, 3})
                .build();

        var item = itemDto.toEntity("house-1");

        // The body's uuid is never trusted — ids come from the server (create) or the
        // already-checked path variable (update/complete).
        assertThat(item.id(), is(nullValue()));
        assertThat(item.houseId(), is("house-1"));
        assertThat(item.name(), is(itemDto.getName()));
        assertThat(item.brand(), is(itemDto.getBrand()));
        // A short id passes through unchanged.
        assertThat(item.measure(), is("g"));
        assertThat(item.amount(), is(itemDto.getAmount()));
        assertThat(item.remaining(), is(itemDto.getRemaining()));
        assertThat(item.expiration(), is(itemDto.getExpiration()));
        assertThat(item.barcode(), is(itemDto.getBarcode()));
        assertThat(item.image(), is(equalTo(itemDto.getImage())));
    }

    @Test
    @DisplayName("a valid item has no constraint violations")
    void validItemHasNoViolations(final Validator validator) {
        final var itemDto = validDto().build();

        assertThat(validator.validate(itemDto), empty());
    }

    @Test
    @DisplayName("a zero amount is rejected")
    void zeroAmountIsRejected(final Validator validator) {
        final var itemDto = validDto().amount(0d).build();

        assertThat(validator.validate(itemDto), not(empty()));
    }

    @Test
    @DisplayName("a negative remaining is rejected")
    void negativeRemainingIsRejected(final Validator validator) {
        final var itemDto = validDto().remaining(-1d).build();

        assertThat(validator.validate(itemDto), not(empty()));
    }

    @Test
    @DisplayName("a name longer than 200 characters is rejected")
    void overlongNameIsRejected(final Validator validator) {
        final var itemDto = validDto().name("a".repeat(201)).build();

        assertThat(validator.validate(itemDto), not(empty()));
    }

    @Test
    @DisplayName("a brand longer than 200 characters is rejected")
    void overlongBrandIsRejected(final Validator validator) {
        final var itemDto = validDto().brand("a".repeat(201)).build();

        assertThat(validator.validate(itemDto), not(empty()));
    }

    @Test
    @DisplayName("an invalid barcode is rejected")
    void invalidBarcodeIsRejected(final Validator validator) {
        final var itemDto = validDto().barcode("abc123").build();

        assertThat(validator.validate(itemDto), not(empty()));
    }

    @Test
    @DisplayName("an empty barcode is accepted")
    void emptyBarcodeIsAccepted(final Validator validator) {
        final var itemDto = validDto().barcode("").build();

        assertThat(validator.validate(itemDto), empty());
    }

    private PantryItemDto.PantryItemDtoBuilder validDto() {
        return PantryItemDto.builder().name("bread").measure("item").amount(10d).remaining(10d);
    }
}
