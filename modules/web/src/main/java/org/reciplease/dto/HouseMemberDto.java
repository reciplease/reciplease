package org.reciplease.dto;

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
    String userId;
    // Members who haven't set a display handle yet are represented with a null
    // handle on the wire, not an empty string.
    @Schema(nullable = true)
    String handle;
    HouseRole role;

    public static HouseMemberDto from(final HouseMembership membership) {
        return HouseMemberDto.builder()
                .userId(membership.userId())
                .handle(membership.handle())
                .role(membership.role())
                .build();
    }
}
