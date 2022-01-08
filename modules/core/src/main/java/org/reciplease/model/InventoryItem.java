package org.reciplease.model;

import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;

@Value
public class InventoryItem extends BaseModel {
    @NonNull
    private Ingredient ingredient;
    @NonNull
    private Double amount;
    @NonNull
    private LocalDate expiration;
}
