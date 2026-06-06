package org.reciplease.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Records that a given amount was drawn from a particular {@link InventoryItem} to satisfy a
 * recipe ingredient when a recipe was planned. The {@link #barcode} is snapshotted at planning
 * time so that suggestions still work after the inventory item has been consumed or deleted.
 */
@Value
@Builder
public class InventoryAllocation {
    @NonNull
    String inventoryItemId;

    String barcode;

    @NonNull
    Double amount;
}
