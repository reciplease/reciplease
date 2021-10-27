package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class InventoryItemDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID uuid;
    UUID ingredientUuid;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Measure measure;
    Double amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;

    public static InventoryItemDto from(final InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .uuid(inventoryItem.getUuid())
                .ingredientUuid(inventoryItem.getIngredient().getUuid())
                .name(inventoryItem.getIngredient().getName())
                .measure(inventoryItem.getIngredient().getMeasure())
                .amount(inventoryItem.getAmount())
                .expiration(inventoryItem.getExpiration())
                .build();
    }

    public InventoryItem toEntity() {
        return InventoryItem.builder()
                .uuid(uuid)
                .ingredient(Ingredient.builder()
                        .uuid(ingredientUuid)
                        .name(name)
                        .measure(measure)
                        .build())
                .amount(amount)
                .expiration(expiration)
                .build();
    }
}
