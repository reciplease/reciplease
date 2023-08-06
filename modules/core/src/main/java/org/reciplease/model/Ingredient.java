package org.reciplease.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;


@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Ingredient extends Identifiable {

    @NotNull
    @NotBlank
    String name;

    @NotNull
    Measure measure;
}
