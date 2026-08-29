package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.ApiKey;
import org.reciplease.model.HouseRole;

/** The owner-facing listing shape for an {@link ApiKey} — never carries the raw secret. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "ApiKey")
public class ApiKeyDto {
    @Schema(requiredMode = REQUIRED)
    String id;

    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(requiredMode = REQUIRED)
    HouseRole role;

    @Schema(requiredMode = REQUIRED)
    String keyPrefix;

    @Schema(requiredMode = REQUIRED)
    Instant createdAt;
    // Null until the key is used for the first time.
    @Schema(nullable = true)
    Instant lastUsedAt;

    public static ApiKeyDto from(final ApiKey apiKey) {
        return ApiKeyDto.builder()
                .id(apiKey.id())
                .name(apiKey.name())
                .role(apiKey.role())
                .keyPrefix(apiKey.keyPrefix())
                .createdAt(apiKey.createdAt())
                .lastUsedAt(apiKey.lastUsedAt())
                .build();
    }
}
