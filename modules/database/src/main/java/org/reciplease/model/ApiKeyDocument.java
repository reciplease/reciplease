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

@Document("api_keys")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyDocument {

    @Id
    private String id;

    private String houseId;
    private String name;
    private String role;
    private String createdByUserId;

    @Indexed(unique = true)
    private String keyPrefix;

    private String keyHash;

    @CreatedDate
    private Instant createdAt;

    private Instant lastUsedAt;

    public static ApiKeyDocument from(final ApiKey apiKey) {
        return ApiKeyDocument.builder()
                .id(apiKey.id())
                .houseId(apiKey.houseId())
                .name(apiKey.name())
                .role(apiKey.role() != null ? apiKey.role().name() : null)
                .createdByUserId(apiKey.createdByUserId())
                .keyPrefix(apiKey.keyPrefix())
                .keyHash(apiKey.keyHash())
                .createdAt(apiKey.createdAt())
                .lastUsedAt(apiKey.lastUsedAt())
                .build();
    }

    public ApiKey toModel() {
        return new ApiKey(
                id,
                houseId,
                name,
                role != null ? HouseRole.valueOf(role) : null,
                createdByUserId,
                keyPrefix,
                keyHash,
                createdAt,
                lastUsedAt);
    }
}
