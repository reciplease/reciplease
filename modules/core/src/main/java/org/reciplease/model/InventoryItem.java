package org.reciplease.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Value
@SuperBuilder(toBuilder = true)
public class InventoryItem extends BaseModel {
    @NonNull
    private Ingredient ingredient;
    @NonNull
    private Double amount;
    @NonNull
    private LocalDate expiration;
}
