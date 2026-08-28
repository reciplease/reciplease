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
import java.time.LocalDate;

@Document("pantry")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemDocument {

    @Id
    private String id;
    private String houseId;
    private String name;
    private String brand;
    private String measure;
    private Double amount;
    private Double remaining;
    private LocalDate expiration;
    private String barcode;
    private byte[] image;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public static PantryItemDocument from(final PantryItem item) {
        return PantryItemDocument.builder()
                .id(item.id())
                .houseId(item.houseId())
                .name(item.name())
                .brand(item.brand())
                .measure(item.measure())
                .amount(item.amount())
                .remaining(item.remaining())
                .expiration(item.expiration())
                .barcode(item.barcode())
                .image(item.image())
                .createdBy(item.createdBy())
                .createdAt(item.createdAt())
                .updatedAt(item.updatedAt())
                .build();
    }

    // Documents saved before `remaining` existed have none stored; PantryItem's own
    // constructor defaults a null `remaining` to `amount` (fully stocked), so nothing extra
    // is needed here. Same for `brand` — simply null on documents saved before it existed.
    public PantryItem toModel() {
        return new PantryItem(id, createdBy, houseId, name, brand, measure, amount, remaining, expiration, barcode, image, createdAt, updatedAt);
    }
}
