package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A snapshot of an {@link PantryItemDocument} taken right before it's deleted (binned, eaten
 * down to nothing, or explicitly removed) — history only, nothing in core reads these back.
 */
@Document("pantryArchive")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedPantryItemDocument {

    @Id
    private String id;
    private String originalId;
    private String houseId;
    private String name;
    private String brand;
    private String measure;
    private Double amount;
    private Double remaining;
    private LocalDate expiration;
    private String barcode;
    private byte[] image;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant archivedAt;

    public static ArchivedPantryItemDocument from(final PantryItemDocument item, final Instant archivedAt) {
        return ArchivedPantryItemDocument.builder()
                .originalId(item.getId())
                .houseId(item.getHouseId())
                .name(item.getName())
                .brand(item.getBrand())
                .measure(item.getMeasure())
                .amount(item.getAmount())
                .remaining(item.getRemaining())
                .expiration(item.getExpiration())
                .barcode(item.getBarcode())
                .image(item.getImage())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .archivedAt(archivedAt)
                .build();
    }
}
