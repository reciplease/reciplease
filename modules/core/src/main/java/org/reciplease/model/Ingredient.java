package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
