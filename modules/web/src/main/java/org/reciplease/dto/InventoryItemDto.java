package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class InventoryItemDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String uuid;
    String name;
    String measure;
    Double amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;
    String barcode;

    public static InventoryItemDto from(final InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .uuid(inventoryItem.getId())
                .name(inventoryItem.getName())
                .measure(Measure.normalizeId(inventoryItem.getMeasure()))
                .amount(inventoryItem.getAmount())
                .expiration(inventoryItem.getExpiration())
                .barcode(inventoryItem.getBarcode())
                .build();
    }

    public InventoryItem toEntity() {
        return InventoryItem.builder()
                .id(uuid)
                .name(name)
                .measure(Measure.normalizeId(measure))
                .amount(amount)
                .expiration(expiration)
                .barcode(barcode)
                .build();
    }
}
