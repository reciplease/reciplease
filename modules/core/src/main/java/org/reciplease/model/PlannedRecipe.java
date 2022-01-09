package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;

@Value
@AllArgsConstructor
@Builder
public class PlannedRecipe {
    @NonNull
    Recipe recipe;
    @NonNull
    LocalDate date;
}
