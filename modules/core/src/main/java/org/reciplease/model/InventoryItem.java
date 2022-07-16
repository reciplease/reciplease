package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class InventoryItem extends Identifiable {
    @NonNull
    Ingredient ingredient;
    @NonNull
    Double amount;
    @NonNull
    LocalDate expiration;
}
