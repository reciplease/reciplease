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
    private String name;
    private String measure;
    private Double amount;
    private LocalDate expiration;
    private String barcode;

    public static InventoryItemDocument from(final InventoryItem item) {
        return InventoryItemDocument.builder()
                .id(item.getId())
                .name(item.getName())
                .measure(item.getMeasure())
                .amount(item.getAmount())
                .expiration(item.getExpiration())
                .barcode(item.getBarcode())
                .build();
    }

    public InventoryItem toModel() {
        return InventoryItem.builder()
                .id(id)
                .name(name)
                .measure(measure)
                .amount(amount)
                .expiration(expiration)
                .barcode(barcode)
                .build();
    }
}
