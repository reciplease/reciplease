package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PantryAllocationDocument {

    private String pantryItemId;
    private String barcode;
    private Double amount;

    public static PantryAllocationDocument from(final PantryAllocation allocation) {
        return PantryAllocationDocument.builder()
                .pantryItemId(allocation.pantryItemId())
                .barcode(allocation.barcode())
                .amount(allocation.amount())
                .build();
    }

    public PantryAllocation toModel() {
        return new PantryAllocation(pantryItemId, barcode, amount);
    }
}
