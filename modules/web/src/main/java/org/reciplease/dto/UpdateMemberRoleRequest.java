package org.reciplease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.reciplease.model.HouseRole;

@Value
public class UpdateMemberRoleRequest {
    @NotNull
    HouseRole role;
}
