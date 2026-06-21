package org.reciplease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.reciplease.model.HouseRole;

@Value
public class CreateInviteRequest {
    @NotNull
    HouseRole role;
}
