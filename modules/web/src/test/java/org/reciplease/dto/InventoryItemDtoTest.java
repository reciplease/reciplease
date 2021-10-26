package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class InventoryItemDtoTest {
    @Test
    @DisplayName("create DTO from entity")
    void from() {
        final var ingredient = Ingredient.builder()
                .randomUUID()
                .build();

        final var item = InventoryItem.builder()
                .ingredient(ingredient)
                .amount(10d)
                .expiration(LocalDate.now())
                .build();

        final var itemDto = InventoryItemDto.from(item);

        assertThat(itemDto.getUuid(), is(item.getUuid()));
        assertThat(itemDto.getIngredientUuid(), is(ingredient.getUuid()));
        assertThat(itemDto.getAmount(), is(item.getAmount()));
        assertThat(itemDto.getExpiration(), is(item.getExpiration()));
    }

    @Test
    @DisplayName("create entity from DTO")
    void toEntity() {
        final var itemDto = InventoryItemDto.builder()
                .uuid(UUID.randomUUID())
                .ingredientUuid(UUID.randomUUID())
                .amount(10d)
                .expiration(LocalDate.now())
                .build();

        final var item = itemDto.toEntity();

        assertThat(item.getUuid(), is(itemDto.getUuid()));
        assertThat(item.getIngredient().getUuid(), is(itemDto.getIngredientUuid()));
        assertThat(item.getAmount(), is(itemDto.getAmount()));
        assertThat(item.getExpiration(), is(itemDto.getExpiration()));
     }
}