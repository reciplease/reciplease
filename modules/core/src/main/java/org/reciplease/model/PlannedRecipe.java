package org.reciplease.model;

import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;

@Value
public class PlannedRecipe extends BaseModel {
    @NonNull
    private Recipe recipe;
    @NonNull
    private LocalDate date;
}
