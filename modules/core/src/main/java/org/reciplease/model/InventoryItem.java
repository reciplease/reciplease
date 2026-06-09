package org.reciplease.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A physical item in the pantry. Independent of any recipe — it carries its own name and
 * measure rather than referencing a shared catalog. The optional {@link #barcode} (the digits
 * encoded by a scanned barcode) lets historic planned recipes suggest matching items when a
 * recipe is planned again.
 */
public record InventoryItem(
        String id,
        String createdBy,
        String name,
        String measure,
        Double amount,
        LocalDate expiration,
        String barcode) implements Audited {

    public InventoryItem {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(measure, "measure");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(expiration, "expiration");
    }

    public InventoryItem withId(final String id) {
        return new InventoryItem(id, createdBy, name, measure, amount, expiration, barcode);
    }
}
