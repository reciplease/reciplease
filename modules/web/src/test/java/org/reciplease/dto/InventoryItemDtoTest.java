package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.InventoryItem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class InventoryItemDtoTest {
    @Test
    @DisplayName("create DTO from entity")
    void from() {
        var item = new InventoryItem(UUID.randomUUID().toString(), null, "house-1", "bread", "ITEMS", 10d, 12d, LocalDate.now(), "0123456789012",
                new byte[]{1, 2, 3}, Instant.now(), Instant.now());

        var itemDto = InventoryItemDto.from(item);

        assertThat(itemDto.getUuid(), is(item.id()));
        assertThat(itemDto.getHouseId(), is(item.houseId()));
        assertThat(itemDto.getName(), is(item.name()));
        // Legacy measure ids are normalized to their short form.
        assertThat(itemDto.getMeasure(), is("item"));
        assertThat(itemDto.getAmount(), is(item.amount()));
        assertThat(itemDto.getRemaining(), is(item.remaining()));
        assertThat(itemDto.getExpiration(), is(item.expiration()));
        assertThat(itemDto.getBarcode(), is(item.barcode()));
        assertThat(itemDto.getImage(), is(equalTo(item.image())));
        assertThat(itemDto.getUpdatedAt(), is(item.updatedAt()));
    }

    @Test
    @DisplayName("create DTO from entity with no image")
    void fromWithNoImage() {
        var item = new InventoryItem(UUID.randomUUID().toString(), null, "house-1", "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012");

        var itemDto = InventoryItemDto.from(item);

        assertThat(itemDto.getImage(), is(nullValue()));
    }

    @Test
    @DisplayName("create entity from DTO")
    void toEntity() {
        var itemDto = InventoryItemDto.builder()
                .uuid(UUID.randomUUID().toString())
                .name("bread")
                .measure("g")
                .amount(10d)
                .remaining(6d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .image(new byte[]{1, 2, 3})
                .build();

        var item = itemDto.toEntity("house-1");

        // The body's uuid is never trusted — ids come from the server (create) or the
        // already-checked path variable (update/complete).
        assertThat(item.id(), is(nullValue()));
        assertThat(item.houseId(), is("house-1"));
        assertThat(item.name(), is(itemDto.getName()));
        // A short id passes through unchanged.
        assertThat(item.measure(), is("g"));
        assertThat(item.amount(), is(itemDto.getAmount()));
        assertThat(item.remaining(), is(itemDto.getRemaining()));
        assertThat(item.expiration(), is(itemDto.getExpiration()));
        assertThat(item.barcode(), is(itemDto.getBarcode()));
        assertThat(item.image(), is(equalTo(itemDto.getImage())));
    }
}
