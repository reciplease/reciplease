package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;

/** Owner-facing pending-invite shape, distinct from the public preview {@link InviteDto}. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "HouseInvite")
public class HouseInviteDto {
    @Schema(requiredMode = REQUIRED)
    String id;

    @Schema(requiredMode = REQUIRED)
    String code;

    @Schema(requiredMode = REQUIRED)
    HouseRole role;

    @Schema(requiredMode = REQUIRED)
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
