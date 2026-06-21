package org.reciplease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.House;

/** Invite-preview response: just enough for the landing page to say "you're invited to X". */
@Value
@AllArgsConstructor
@Builder
public class InviteDto {
    String houseId;
    String houseName;

    public static InviteDto from(final House house) {
        return InviteDto.builder()
                .houseId(house.id())
                .houseName(house.name())
                .build();
    }
}
