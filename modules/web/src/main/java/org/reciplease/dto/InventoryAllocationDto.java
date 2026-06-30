package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.InventoryAllocation;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "InventoryAllocation")
public class InventoryAllocationDto {

    String inventoryItemId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String barcode;
    Double amount;

    public static InventoryAllocationDto from(final InventoryAllocation allocation) {
        return InventoryAllocationDto.builder()
                .inventoryItemId(allocation.inventoryItemId())
                .barcode(allocation.barcode())
                .amount(allocation.amount())
                .build();
    }

    public InventoryAllocation toModel() {
        return new InventoryAllocation(inventoryItemId, barcode, amount);
    }
}
