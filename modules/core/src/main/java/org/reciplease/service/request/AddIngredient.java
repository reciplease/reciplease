package org.reciplease.service.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class AddIngredient {
    @NotBlank
    String name;

    @NotBlank
    String measure;

    @DecimalMin(value = "0", inclusive = false)
    Double amount;
}
