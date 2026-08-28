package org.reciplease.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * A shopping-trip capture awaiting digitisation into an {@link PantryItem}. Created during
 * the fast "add a whole shop" loop, which records at most a photo of the barcode plus photos
 * of the printed expiration date and the pack measure/size — every field is optional because each
 * capture step can be skipped. The barcode itself is decoded later, during processing, rather
 * than in real time while shopping. The item's existence is what marks it as pending; completing
 * it creates a real {@link PantryItem} and deletes this one.
 */
public record PendingPantryItem(
        String id,
        String createdBy,
        String houseId,
        byte[] barcodeImage,
        // A decoded barcode from before the capture flow switched from live scanning to a photo
        // (see barcodeImage). Read-only: new captures never set this, only old Mongo documents
        // still have it. Kept so those items aren't left without any barcode at all — the frontend
        // uses it in place of decoding a photo when there isn't one. Remove once no pending items
        // predating that change are left in the collection.
        String legacyBarcode,
        byte[] expirationImage,
        byte[] measureImage,
        Instant createdAt,
        Instant updatedAt) implements Audited, HouseScoped {

    public PendingPantryItem(final String id, final String createdBy, final String houseId, final byte[] barcodeImage,
                                 final byte[] expirationImage, final byte[] measureImage) {
        this(id, createdBy, houseId, barcodeImage, null, expirationImage, measureImage, null, null);
    }

    public PendingPantryItem withId(final String id) {
        return new PendingPantryItem(id, createdBy, houseId, barcodeImage, legacyBarcode, expirationImage, measureImage, createdAt, updatedAt);
    }

    // The generated record equals()/hashCode() would compare the image fields by array identity
    // rather than content; override both so two items with equal image bytes are considered equal.
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PendingPantryItem other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(createdBy, other.createdBy)
                && Objects.equals(houseId, other.houseId)
                && Arrays.equals(barcodeImage, other.barcodeImage)
                && Objects.equals(legacyBarcode, other.legacyBarcode)
                && Arrays.equals(expirationImage, other.expirationImage)
                && Arrays.equals(measureImage, other.measureImage)
                && Objects.equals(createdAt, other.createdAt)
                && Objects.equals(updatedAt, other.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdBy, houseId, Arrays.hashCode(barcodeImage), legacyBarcode, Arrays.hashCode(expirationImage), Arrays.hashCode(measureImage), createdAt, updatedAt);
    }
}
