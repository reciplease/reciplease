package org.reciplease.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * A shopping-trip capture awaiting digitisation into an {@link InventoryItem}. Created during
 * the fast "add a whole shop" loop, which records at most a scanned {@link #barcode} plus photos
 * of the printed expiration date and the pack measure/size — every field is optional because each
 * capture step can be skipped. The item's existence is what marks it as pending; completing it
 * creates a real {@link InventoryItem} and deletes this one.
 */
public record PendingInventoryItem(
        String id,
        String createdBy,
        String houseId,
        String barcode,
        byte[] expirationImage,
        byte[] measureImage,
        Instant createdAt,
        Instant updatedAt) implements Audited, HouseScoped {

    public PendingInventoryItem(final String id, final String createdBy, final String houseId, final String barcode,
                                 final byte[] expirationImage, final byte[] measureImage) {
        this(id, createdBy, houseId, barcode, expirationImage, measureImage, null, null);
    }

    public PendingInventoryItem withId(final String id) {
        return new PendingInventoryItem(id, createdBy, houseId, barcode, expirationImage, measureImage, createdAt, updatedAt);
    }

    // The generated record equals()/hashCode() would compare the image fields by array identity
    // rather than content; override both so two items with equal image bytes are considered equal.
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PendingInventoryItem other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(createdBy, other.createdBy)
                && Objects.equals(houseId, other.houseId)
                && Objects.equals(barcode, other.barcode)
                && Arrays.equals(expirationImage, other.expirationImage)
                && Arrays.equals(measureImage, other.measureImage)
                && Objects.equals(createdAt, other.createdAt)
                && Objects.equals(updatedAt, other.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdBy, houseId, barcode, Arrays.hashCode(expirationImage), Arrays.hashCode(measureImage), createdAt, updatedAt);
    }
}
