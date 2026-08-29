package org.reciplease.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A user's linked Google Health account. {@code userId} is the {@code @Id} — one connection
 * per Reciplease user, unlike {@link PasskeyCredentialDocument} which is keyed by credential id.
 * <p>
 * {@code createdAt}/{@code updatedAt} are stamped by {@link org.reciplease.service.GoogleHealthAdapter}
 * (via its injected {@code Clock}), not Spring Data auditing: auditing's default "is this new"
 * check treats an entity as new only when its {@code @Id} is null, but {@code userId} here is
 * always manually assigned before save, so {@code @CreatedDate} would never fire on first insert.
 */
@Document("google_health_connections")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleHealthConnectionDocument {

    @Id
    private String userId;

    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private String scope;
    private Instant createdAt;
    private Instant updatedAt;

    public static GoogleHealthConnectionDocument from(final GoogleHealthConnection connection) {
        return GoogleHealthConnectionDocument.builder()
                .userId(connection.userId())
                .accessToken(connection.accessToken())
                .refreshToken(connection.refreshToken())
                .expiresAt(connection.expiresAt())
                .scope(connection.scope())
                .createdAt(connection.createdAt())
                .updatedAt(connection.updatedAt())
                .build();
    }

    public GoogleHealthConnection toModel() {
        return new GoogleHealthConnection(userId, accessToken, refreshToken, expiresAt, scope, createdAt, updatedAt);
    }
}
