package org.reciplease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.reciplease.model.HouseRole;

@Value
public class CreateApiKeyRequest {
    @NotBlank
    String name;
    @NotNull
    HouseRole role;
}
