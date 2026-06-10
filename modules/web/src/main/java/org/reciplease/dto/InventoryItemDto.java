package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class InventoryItemDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String uuid;
    String name;
    String measure;
    Double amount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;
    String barcode;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Instant updatedAt;

    public static InventoryItemDto from(final InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .uuid(inventoryItem.id())
                .name(inventoryItem.name())
                .measure(Measure.normalizeId(inventoryItem.measure()))
                .amount(inventoryItem.amount())
                .expiration(inventoryItem.expiration())
                .barcode(inventoryItem.barcode())
                .updatedAt(inventoryItem.updatedAt())
                .build();
    }

    public InventoryItem toEntity() {
        return new InventoryItem(uuid, null, name, Measure.normalizeId(measure), amount, expiration, barcode);
    }
}
