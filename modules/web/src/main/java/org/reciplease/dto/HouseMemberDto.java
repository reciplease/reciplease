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

    /**
     * @param isSelf whether this membership is the caller's own — their email is
     *               shown in full; every other member's is masked (see {@link
     *               org.reciplease.model.Email#masked()}).
     */
    public static HouseMemberDto from(final HouseMembership membership, final boolean isSelf) {
        return HouseMemberDto.builder()
                .userId(membership.userId())
                .email((isSelf ? membership.email() : membership.email().masked()).value())
                .role(membership.role())
                .build();
    }
}
