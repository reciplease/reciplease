package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.CreatedApiKey;
import org.reciplease.model.HouseRole;

import java.time.Instant;

/** Returned only once, immediately after creation — {@code rawKey} can never be recovered afterwards. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "CreatedApiKey")
public class CreatedApiKeyDto {
    String id;
    String name;
    HouseRole role;
    String rawKey;
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
