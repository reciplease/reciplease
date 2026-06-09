package org.reciplease.model;

import java.util.Objects;

/**
 * Records that a given amount was drawn from a particular {@link InventoryItem} to satisfy a
 * recipe ingredient when a recipe was planned. The {@link #barcode} is snapshotted at planning
 * time so that suggestions still work after the inventory item has been consumed or deleted.
 */
public record InventoryAllocation(String inventoryItemId, String barcode, Double amount) {
    public InventoryAllocation {
        Objects.requireNonNull(inventoryItemId, "inventoryItemId");
        Objects.requireNonNull(amount, "amount");
    }
}
