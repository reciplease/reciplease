package org.reciplease.model;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class Ingredient extends BaseModel {
    @NotNull
    @NotBlank
    private String name;

    @NotNull
    private Measure measure;
}
