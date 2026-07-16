package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("pendingInventory")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PendingInventoryItemDocument {

    @Id
    private String id;
    private String houseId;
    private String barcode;
    private byte[] expirationImage;
    private byte[] measureImage;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public static PendingInventoryItemDocument from(final PendingInventoryItem item) {
        return PendingInventoryItemDocument.builder()
                .id(item.id())
                .houseId(item.houseId())
                .barcode(item.barcode())
                .expirationImage(item.expirationImage())
                .measureImage(item.measureImage())
                .createdBy(item.createdBy())
                .createdAt(item.createdAt())
                .updatedAt(item.updatedAt())
                .build();
    }

    public PendingInventoryItem toModel() {
        return new PendingInventoryItem(id, createdBy, houseId, barcode, expirationImage, measureImage, createdAt, updatedAt);
    }
}
