package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(name = "InventoryItem")
public class InventoryItemDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String uuid;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String houseId;
    String name;
    String measure;
    Double amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;
    String barcode;
    byte[] image;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Instant updatedAt;

    public static InventoryItemDto from(final InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .uuid(inventoryItem.id())
                .houseId(inventoryItem.houseId())
                .name(inventoryItem.name())
                .measure(Measure.normalizeId(inventoryItem.measure()))
                .amount(inventoryItem.amount())
                .expiration(inventoryItem.expiration())
                .barcode(inventoryItem.barcode())
                .image(inventoryItem.image())
                .updatedAt(inventoryItem.updatedAt())
                .build();
    }

    // houseId is supplied by the controller from the already-validated X-RCPLS-House-Id
    // header, not trusted from the request body, so a member can't write into a house
    // they don't belong to by spoofing the field.
    public InventoryItem toEntity(final String houseId) {
        return new InventoryItem(uuid, null, houseId, name, Measure.normalizeId(measure), amount, expiration, barcode, image);
    }
}
