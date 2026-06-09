package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
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
    @CreatedBy
    private String createdBy;

    public static InventoryItemDocument from(final InventoryItem item) {
        return InventoryItemDocument.builder()
                .id(item.id())
                .name(item.name())
                .measure(item.measure())
                .amount(item.amount())
                .expiration(item.expiration())
                .barcode(item.barcode())
                .createdBy(item.createdBy())
                .build();
    }

    public InventoryItem toModel() {
        return new InventoryItem(id, createdBy, name, measure, amount, expiration, barcode);
    }
}
