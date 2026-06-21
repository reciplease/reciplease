package org.reciplease.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
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
        String houseId,
        String name,
        String measure,
        Double amount,
        LocalDate expiration,
        String barcode,
        byte[] image,
        Instant createdAt,
        Instant updatedAt) implements Audited, HouseScoped {

    public InventoryItem {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(measure, "measure");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(expiration, "expiration");
    }

    public InventoryItem(final String id, final String createdBy, final String houseId, final String name, final String measure,
                          final Double amount, final LocalDate expiration, final String barcode) {
        this(id, createdBy, houseId, name, measure, amount, expiration, barcode, null, null, null);
    }

    public InventoryItem(final String id, final String createdBy, final String houseId, final String name, final String measure,
                          final Double amount, final LocalDate expiration, final String barcode, final byte[] image) {
        this(id, createdBy, houseId, name, measure, amount, expiration, barcode, image, null, null);
    }

    public InventoryItem withId(final String id) {
        return new InventoryItem(id, createdBy, houseId, name, measure, amount, expiration, barcode, image, createdAt, updatedAt);
    }

    // The generated record equals()/hashCode() would compare `image` by array identity rather
    // than content; override both so two items with equal image bytes are considered equal.
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InventoryItem other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(createdBy, other.createdBy)
                && Objects.equals(houseId, other.houseId)
                && Objects.equals(name, other.name)
                && Objects.equals(measure, other.measure)
                && Objects.equals(amount, other.amount)
                && Objects.equals(expiration, other.expiration)
                && Objects.equals(barcode, other.barcode)
                && Arrays.equals(image, other.image)
                && Objects.equals(createdAt, other.createdAt)
                && Objects.equals(updatedAt, other.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdBy, houseId, name, measure, amount, expiration, barcode, Arrays.hashCode(image), createdAt, updatedAt);
    }
}
