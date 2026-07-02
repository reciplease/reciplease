package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A user's linked Fitbit account. {@code userId} is the {@code @Id} — one connection per
 * Reciplease user, unlike {@link PasskeyCredentialDocument} which is keyed by credential id.
 * <p>
 * {@code createdAt}/{@code updatedAt} are stamped by {@link org.reciplease.service.FitbitService}
 * (via its injected {@code Clock}), not Spring Data auditing: auditing's default "is this new"
 * check treats an entity as new only when its {@code @Id} is null, but {@code userId} here is
 * always manually assigned before save, so {@code @CreatedDate} would never fire on first insert.
 */
@Document("fitbit_connections")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FitbitConnectionDocument {

    @Id
    private String userId;
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private String fitbitUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public static FitbitConnectionDocument from(final FitbitConnection connection) {
        return FitbitConnectionDocument.builder()
                .userId(connection.userId())
                .accessToken(connection.accessToken())
                .refreshToken(connection.refreshToken())
                .expiresAt(connection.expiresAt())
                .fitbitUserId(connection.fitbitUserId())
                .createdAt(connection.createdAt())
                .updatedAt(connection.updatedAt())
                .build();
    }

    public FitbitConnection toModel() {
        return new FitbitConnection(userId, accessToken, refreshToken, expiresAt, fitbitUserId, createdAt, updatedAt);
    }
}
