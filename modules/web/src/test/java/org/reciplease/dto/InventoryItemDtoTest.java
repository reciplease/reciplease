package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.InventoryItem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class InventoryItemDtoTest {
    @Test
    @DisplayName("create DTO from entity")
    void from() {
        var item = new InventoryItem(UUID.randomUUID().toString(), null, "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012",
                Instant.now(), Instant.now());

        var itemDto = InventoryItemDto.from(item);

        assertThat(itemDto.getUuid(), is(item.id()));
        assertThat(itemDto.getName(), is(item.name()));
        // Legacy measure ids are normalized to their short form.
        assertThat(itemDto.getMeasure(), is("item"));
        assertThat(itemDto.getAmount(), is(item.amount()));
        assertThat(itemDto.getExpiration(), is(item.expiration()));
        assertThat(itemDto.getBarcode(), is(item.barcode()));
        assertThat(itemDto.getUpdatedAt(), is(item.updatedAt()));
    }

    @Test
    @DisplayName("create entity from DTO")
    void toEntity() {
        var itemDto = InventoryItemDto.builder()
                .uuid(UUID.randomUUID().toString())
                .name("bread")
                .measure("g")
                .amount(10d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .build();

        var item = itemDto.toEntity();

        assertThat(item.id(), is(itemDto.getUuid()));
        assertThat(item.name(), is(itemDto.getName()));
        // A short id passes through unchanged.
        assertThat(item.measure(), is("g"));
        assertThat(item.amount(), is(itemDto.getAmount()));
        assertThat(item.expiration(), is(itemDto.getExpiration()));
        assertThat(item.barcode(), is(itemDto.getBarcode()));
    }
}
