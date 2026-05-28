package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document("inventory")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDocument {

    @Id
    private UUID uuid;
    private UUID ingredientId;
    private String ingredientName;
    private Measure measure;
    private Double amount;
    private LocalDate expiration;

    public static InventoryItemDocument from(final InventoryItem item) {
        return InventoryItemDocument.builder()
                .uuid(item.getUuid() != null ? item.getUuid() : UUID.randomUUID())
                .ingredientId(item.getIngredient().getUuid())
                .ingredientName(item.getIngredient().getName())
                .measure(item.getIngredient().getMeasure())
                .amount(item.getAmount())
                .expiration(item.getExpiration())
                .build();
    }

    public InventoryItem toModel() {
        final Ingredient ingredient = Ingredient.builder()
                .uuid(ingredientId)
                .name(ingredientName)
                .measure(measure)
                .build();
        return InventoryItem.builder()
                .uuid(uuid)
                .ingredient(ingredient)
                .amount(amount)
                .expiration(expiration)
                .build();
    }
}
