package org.reciplease.dto;

import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class InventoryItemDto {
    UUID uuid;
    UUID ingredientUuid;
    Double amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;

    public static InventoryItemDto from(final InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .uuid(inventoryItem.getUuid())
                .ingredientUuid(inventoryItem.getIngredient().getUuid())
                .amount(inventoryItem.getAmount())
                .expiration(inventoryItem.getExpiration())
                .build();
    }

    public InventoryItem toEntity() {
        return InventoryItem.builder()
                .uuid(uuid)
                .ingredient(Ingredient.builder()
                        .uuid(ingredientUuid)
                        .build())
                .amount(amount)
                .expiration(expiration)
                .build();
    }
}
