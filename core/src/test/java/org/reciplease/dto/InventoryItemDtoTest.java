package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;

import javax.persistence.DiscriminatorColumn;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class InventoryItemDtoTest {
    @Test
    @DisplayName("should create DTO from model")
    void factory() {
        final var ingredient = Ingredient.builder()
                .uuid(UUID.randomUUID())
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
}