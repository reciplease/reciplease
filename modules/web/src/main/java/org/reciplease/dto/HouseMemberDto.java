package org.reciplease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;

@Value
@AllArgsConstructor
@Builder
public class HouseMemberDto {
    String userId;
    String email;
    HouseRole role;

    public static HouseMemberDto from(final HouseMembership membership) {
        return HouseMemberDto.builder()
                .userId(membership.userId())
                .email(membership.email().value())
                .role(membership.role())
                .build();
    }
}
