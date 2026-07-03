package org.reciplease.model;

import java.time.Instant;

/**
 * A user's linked Google Health account: the OAuth2 tokens needed to call the Google Health
 * API on their behalf, persisted server-side (not just in the browser) since the frontend is
 * stateless and tokens must be refreshable across sessions. There is at most one connection
 * per Reciplease {@code userId} — see {@link org.reciplease.repository.GoogleHealthConnectionRepository}.
 */
public record GoogleHealthConnection(
        String userId,
        String accessToken,
        String refreshToken,
        Instant expiresAt,
        String scope,
        Instant createdAt,
        Instant updatedAt) {}
