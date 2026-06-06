package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.InventoryAllocation;

@Value
@AllArgsConstructor
@Builder
public class InventoryAllocationDto {

    String inventoryItemId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String barcode;
    Double amount;

    public static InventoryAllocationDto from(final InventoryAllocation allocation) {
        return InventoryAllocationDto.builder()
                .inventoryItemId(allocation.getInventoryItemId())
                .barcode(allocation.getBarcode())
                .amount(allocation.getAmount())
                .build();
    }

    public InventoryAllocation toModel() {
        return InventoryAllocation.builder()
                .inventoryItemId(inventoryItemId)
                .barcode(barcode)
                .amount(amount)
                .build();
    }
}
