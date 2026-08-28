package org.reciplease.model;

import java.util.Objects;

/**
 * Records that a given amount was drawn from a particular {@link PantryItem} to satisfy a
 * recipe ingredient when a recipe was planned. The {@link #barcode} is snapshotted at planning
 * time so that suggestions still work after the pantry item has been consumed or deleted.
 */
public record PantryAllocation(String pantryItemId, String barcode, Double amount) {
    public PantryAllocation {
        Objects.requireNonNull(pantryItemId, "pantryItemId");
        Objects.requireNonNull(amount, "amount");
    }
}
