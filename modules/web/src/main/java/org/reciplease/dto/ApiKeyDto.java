package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.ApiKey;
import org.reciplease.model.HouseRole;

import java.time.Instant;

/** The owner-facing listing shape for an {@link ApiKey} — never carries the raw secret. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "ApiKey")
public class ApiKeyDto {
    String id;
    String name;
    HouseRole role;
    String keyPrefix;
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
