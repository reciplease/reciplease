package org.reciplease.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.PantryItem;

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
}
