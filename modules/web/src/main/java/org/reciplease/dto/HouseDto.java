package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.House;
import org.reciplease.model.HouseRole;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "House")
public class HouseDto {
    String id;
    String name;
    HouseRole role;

    public static HouseDto from(final House house, final HouseRole role) {
        return HouseDto.builder()
                .id(house.id())
                .name(house.name())
                .role(role)
                .build();
    }
}
