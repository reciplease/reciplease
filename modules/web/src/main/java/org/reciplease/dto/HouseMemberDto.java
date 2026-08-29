package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "HouseMember")
public class HouseMemberDto {
    @Schema(requiredMode = REQUIRED)
    String userId;
    // Members who haven't set a display handle yet are represented with a null
    // handle on the wire, not an empty string.
    @Schema(nullable = true)
    String handle;

    @Schema(requiredMode = REQUIRED)
    HouseRole role;

    public static HouseMemberDto from(final HouseMembership membership) {
        return HouseMemberDto.builder()
                .userId(membership.userId())
                .handle(membership.handle())
                .role(membership.role())
                .build();
    }
}
