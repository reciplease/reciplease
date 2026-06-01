package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("inventory")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDocument {

    @Id
    private String id;
    private String ingredientId;
    private String ingredientName;
    private String measure;
    private Double amount;
    private LocalDate expiration;

    public static InventoryItemDocument from(final InventoryItem item) {
        return InventoryItemDocument.builder()
                .id(item.getId())
                .ingredientId(item.getIngredient().getId())
                .ingredientName(item.getIngredient().getName())
                .measure(item.getIngredient().getMeasure())
                .amount(item.getAmount())
                .expiration(item.getExpiration())
                .build();
    }

    public InventoryItem toModel() {
        final Ingredient ingredient = Ingredient.builder()
                .id(ingredientId)
                .name(ingredientName)
                .measure(measure)
                .build();
        return InventoryItem.builder()
                .id(id)
                .ingredient(ingredient)
                .amount(amount)
                .expiration(expiration)
                .build();
    }
}
