package org.reciplease.model;

import lombok.Value;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@SuperBuilder(toBuilder = true)
public class Ingredient extends Identifiable {
    @NotNull
    @NotBlank
    String name;

    @NotNull
    Measure measure;
}
