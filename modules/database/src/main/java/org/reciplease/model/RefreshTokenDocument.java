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

@Document("refresh_tokens")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDocument {

    @Id
    private String id;

    private String userId;
    private String familyId;

    @Indexed
    private String tokenPrefix;

    private String tokenHash;

    @CreatedDate
    private Instant issuedAt;

    private Instant expiresAt;
    private Instant usedAt;
    private Instant revokedAt;

    public static RefreshTokenDocument from(final RefreshTokenRecord record) {
        return RefreshTokenDocument.builder()
                .id(record.id())
                .userId(record.userId())
                .familyId(record.familyId())
                .tokenPrefix(record.tokenPrefix())
                .tokenHash(record.tokenHash())
                .issuedAt(record.issuedAt())
                .expiresAt(record.expiresAt())
                .usedAt(record.usedAt())
                .revokedAt(record.revokedAt())
                .build();
    }

    public RefreshTokenRecord toModel() {
        return new RefreshTokenRecord(
                id, userId, familyId, tokenPrefix, tokenHash, issuedAt, expiresAt, usedAt, revokedAt);
    }
}
