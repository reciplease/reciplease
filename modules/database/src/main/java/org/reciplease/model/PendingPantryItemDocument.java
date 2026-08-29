package org.reciplease.model;

import java.time.Instant;
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

@Document("pendingPantry")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PendingPantryItemDocument {

    @Id
    private String id;

    private String houseId;
    private byte[] barcodeImage;
    // Predates barcodeImage — a decoded barcode string from when capture scanned live instead of
    // taking a photo. Still named "barcode" in Mongo (the pre-existing field), read here under a
    // clearer name; never written by new saves. See PendingPantryItem.legacyBarcode.
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

    public static PendingPantryItemDocument from(final PendingPantryItem item) {
        return PendingPantryItemDocument.builder()
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

    public PendingPantryItem toModel() {
        return new PendingPantryItem(
                id,
                createdBy,
                houseId,
                barcodeImage,
                legacyBarcode,
                expirationImage,
                measureImage,
                createdAt,
                updatedAt);
    }
}
