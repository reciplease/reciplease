package org.reciplease.model;

import java.time.Instant;

/**
 * A user's linked Fitbit account: the OAuth2 tokens needed to call the Fitbit Web API on
 * their behalf, persisted server-side (not just in the browser) since the frontend is
 * stateless and tokens must be refreshable across sessions. There is at most one connection
 * per Reciplease {@code userId} — see {@link org.reciplease.repository.FitbitConnectionRepository}.
 */
public record FitbitConnection(
        String userId,
        String accessToken,
        String refreshToken,
        Instant expiresAt,
        String fitbitUserId,
        Instant createdAt,
        Instant updatedAt) {}
