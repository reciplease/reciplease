package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;

import java.time.Instant;

/** Owner-facing pending-invite shape, distinct from the public preview {@link InviteDto}. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "HouseInvite")
public class HouseInviteDto {
    String id;
    String code;
    HouseRole role;
    Instant createdAt;

    public static HouseInviteDto from(final Invite invite) {
        return HouseInviteDto.builder()
                .id(invite.id())
                .code(invite.code())
                .role(invite.role())
                .createdAt(invite.createdAt())
                .build();
    }
}
