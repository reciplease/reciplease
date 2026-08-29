package org.reciplease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.reciplease.model.HouseRole;

@Value
public class CreateApiKeyRequest {
    @NotBlank
    @Size(max = 200)
    String name;

    @NotNull
    HouseRole role;
}
