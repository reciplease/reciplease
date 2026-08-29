package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.CreatedApiKey;
import org.reciplease.model.HouseRole;

/** Returned only once, immediately after creation — {@code rawKey} can never be recovered afterwards. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "CreatedApiKey")
public class CreatedApiKeyDto {
    @Schema(requiredMode = REQUIRED)
    String id;

    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(requiredMode = REQUIRED)
    HouseRole role;

    @Schema(requiredMode = REQUIRED)
    String rawKey;

    @Schema(requiredMode = REQUIRED)
    Instant createdAt;

    public static CreatedApiKeyDto from(final CreatedApiKey created) {
        return CreatedApiKeyDto.builder()
                .id(created.apiKey().id())
                .name(created.apiKey().name())
                .role(created.apiKey().role())
                .rawKey(created.rawKey())
                .createdAt(created.apiKey().createdAt())
                .build();
    }
}
