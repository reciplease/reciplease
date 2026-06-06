package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAllocationDocument {

    private String inventoryItemId;
    private String barcode;
    private Double amount;

    public static InventoryAllocationDocument from(final InventoryAllocation allocation) {
        return InventoryAllocationDocument.builder()
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
