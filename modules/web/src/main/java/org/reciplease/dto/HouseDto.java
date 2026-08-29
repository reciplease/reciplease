package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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
    @Schema(requiredMode = REQUIRED)
    String id;

    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(requiredMode = REQUIRED)
    HouseRole role;

    public static HouseDto from(final House house, final HouseRole role) {
        return HouseDto.builder().id(house.id()).name(house.name()).role(role).build();
    }
}
