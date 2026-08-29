package org.reciplease.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

/**
 * A physical item in the pantry. Independent of any recipe — it carries its own name and
 * measure rather than referencing a shared catalog. The optional {@link #barcode} (the digits
 * encoded by a scanned barcode) lets historic planned recipes suggest matching items when a
 * recipe is planned again. {@link #amount} is the full/original quantity (e.g. "bought 12
 * eggs"); {@link #remaining} tracks how much of that is left as it's used up, defaulting to
 * {@link #amount} (fully stocked) whenever it isn't supplied.
 */
public record PantryItem(
        String id,
        String createdBy,
        String houseId,
        String name,
        String brand,
        String measure,
        Double amount,
        Double remaining,
        LocalDate expiration,
        String barcode,
        byte[] image,
        Instant createdAt,
        Instant updatedAt)
        implements Audited, HouseScoped {

    public PantryItem {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(measure, "measure");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(expiration, "expiration");
        if (remaining == null) {
            remaining = amount;
        }
    }

    // Convenience overloads that don't mention `remaining` at all (e.g. for freshly created
    // items, or callers that don't care about the amount/remaining split) — it defaults to
    // `amount` via the compact constructor above.
    public PantryItem(
            final String id,
            final String createdBy,
            final String houseId,
            final String name,
            final String brand,
            final String measure,
            final Double amount,
            final LocalDate expiration,
            final String barcode) {
        this(id, createdBy, houseId, name, brand, measure, amount, null, expiration, barcode, null, null, null);
    }

    public PantryItem(
            final String id,
            final String createdBy,
            final String houseId,
            final String name,
            final String brand,
            final String measure,
            final Double amount,
            final LocalDate expiration,
            final String barcode,
            final byte[] image) {
        this(id, createdBy, houseId, name, brand, measure, amount, null, expiration, barcode, image, null, null);
    }

    // Overloads for callers that do care about the amount/remaining split but not audit fields.
    public PantryItem(
            final String id,
            final String createdBy,
            final String houseId,
            final String name,
            final String brand,
            final String measure,
            final Double amount,
            final Double remaining,
            final LocalDate expiration,
            final String barcode) {
        this(id, createdBy, houseId, name, brand, measure, amount, remaining, expiration, barcode, null, null, null);
    }

    public PantryItem(
            final String id,
            final String createdBy,
            final String houseId,
            final String name,
            final String brand,
            final String measure,
            final Double amount,
            final Double remaining,
            final LocalDate expiration,
            final String barcode,
            final byte[] image) {
        this(id, createdBy, houseId, name, brand, measure, amount, remaining, expiration, barcode, image, null, null);
    }

    public PantryItem withId(final String id) {
        return new PantryItem(
                id,
                createdBy,
                houseId,
                name,
                brand,
                measure,
                amount,
                remaining,
                expiration,
                barcode,
                image,
                createdAt,
                updatedAt);
    }

    public PantryItem withRemaining(final Double remaining) {
        return new PantryItem(
                id,
                createdBy,
                houseId,
                name,
                brand,
                measure,
                amount,
                remaining,
                expiration,
                barcode,
                image,
                createdAt,
                updatedAt);
    }

    // The generated record equals()/hashCode() would compare `image` by array identity rather
    // than content; override both so two items with equal image bytes are considered equal.
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PantryItem other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(createdBy, other.createdBy)
                && Objects.equals(houseId, other.houseId)
                && Objects.equals(name, other.name)
                && Objects.equals(brand, other.brand)
                && Objects.equals(measure, other.measure)
                && Objects.equals(amount, other.amount)
                && Objects.equals(remaining, other.remaining)
                && Objects.equals(expiration, other.expiration)
                && Objects.equals(barcode, other.barcode)
                && Arrays.equals(image, other.image)
                && Objects.equals(createdAt, other.createdAt)
                && Objects.equals(updatedAt, other.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                createdBy,
                houseId,
                name,
                brand,
                measure,
                amount,
                remaining,
                expiration,
                barcode,
                Arrays.hashCode(image),
                createdAt,
                updatedAt);
    }
}
