package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * A physical item in the pantry. Independent of any recipe — it carries its own name and
 * measure rather than referencing a shared catalog. The optional {@link #barcode} (the digits
 * encoded by a scanned barcode) lets historic planned recipes suggest matching items when a
 * recipe is planned again.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class InventoryItem extends Identifiable {
    @NonNull
    String name;
    @NonNull
    String measure;
    @NonNull
    Double amount;
    @NonNull
    LocalDate expiration;
    String barcode;
}
