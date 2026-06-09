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
                .inventoryItemId(allocation.inventoryItemId())
                .barcode(allocation.barcode())
                .amount(allocation.amount())
                .build();
    }

    public InventoryAllocation toModel() {
        return new InventoryAllocation(inventoryItemId, barcode, amount);
    }
}
