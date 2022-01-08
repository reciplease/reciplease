package org.reciplease.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Value
@SuperBuilder(toBuilder = true)
public class PlannedRecipe extends BaseModel {
    @NonNull
    private Recipe recipe;
    @NonNull
    private LocalDate date;
}
