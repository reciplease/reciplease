package org.reciplease.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("invites")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InviteDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String houseId;
    private String role;

    @CreatedDate
    private Instant createdAt;

    private Instant usedAt;
    private String usedByUserId;

    public static InviteDocument from(final Invite invite) {
        return InviteDocument.builder()
                .id(invite.id())
                .code(invite.code())
                .houseId(invite.houseId())
                .role(invite.role() != null ? invite.role().name() : null)
                .createdAt(invite.createdAt())
                .usedAt(invite.usedAt())
                .usedByUserId(invite.usedByUserId())
                .build();
    }

    public Invite toModel() {
        return new Invite(
                id, code, houseId, role != null ? HouseRole.valueOf(role) : null, createdAt, usedAt, usedByUserId);
    }
}
