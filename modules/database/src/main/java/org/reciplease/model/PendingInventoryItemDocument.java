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
import org.springframework.data.mongodb.core.mapping.Field;

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
    private byte[] barcodeImage;
    // Predates barcodeImage — a decoded barcode string from when capture scanned live instead of
    // taking a photo. Still named "barcode" in Mongo (the pre-existing field), read here under a
    // clearer name; never written by new saves. See PendingInventoryItem.legacyBarcode.
    @Field("barcode")
    private String legacyBarcode;
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
                .barcodeImage(item.barcodeImage())
                .legacyBarcode(item.legacyBarcode())
                .expirationImage(item.expirationImage())
                .measureImage(item.measureImage())
                .createdBy(item.createdBy())
                .createdAt(item.createdAt())
                .updatedAt(item.updatedAt())
                .build();
    }

    public PendingInventoryItem toModel() {
        return new PendingInventoryItem(id, createdBy, houseId, barcodeImage, legacyBarcode, expirationImage, measureImage, createdAt, updatedAt);
    }
}
