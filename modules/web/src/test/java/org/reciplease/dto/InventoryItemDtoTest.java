package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class InventoryItemDtoTest {
    @Test
    @DisplayName("create DTO from entity")
    void from() {
        final var item = InventoryItem.builder()
                .id(UUID.randomUUID().toString())
                .name("bread")
                .measure("ITEMS")
                .amount(10d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .build();

        final var itemDto = InventoryItemDto.from(item);

        assertThat(itemDto.getUuid(), is(item.getId()));
        assertThat(itemDto.getName(), is(item.getName()));
        assertThat(itemDto.getMeasure(), is(item.getMeasure()));
        assertThat(itemDto.getAmount(), is(item.getAmount()));
        assertThat(itemDto.getExpiration(), is(item.getExpiration()));
        assertThat(itemDto.getBarcode(), is(item.getBarcode()));
    }

    @Test
    @DisplayName("create entity from DTO")
    void toEntity() {
        final var itemDto = InventoryItemDto.builder()
                .uuid(UUID.randomUUID().toString())
                .name("bread")
                .measure("ITEMS")
                .amount(10d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .build();

        final var item = itemDto.toEntity();

        assertThat(item.getId(), is(itemDto.getUuid()));
        assertThat(item.getName(), is(itemDto.getName()));
        assertThat(item.getMeasure(), is(itemDto.getMeasure()));
        assertThat(item.getAmount(), is(itemDto.getAmount()));
        assertThat(item.getExpiration(), is(itemDto.getExpiration()));
        assertThat(item.getBarcode(), is(itemDto.getBarcode()));
    }
}
