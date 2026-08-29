package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response of {@code GET}/{@code PUT} {@code /api/google-health/connection}.
 * <p>
 * {@code expiresAt} and {@code refreshToken} are only populated when {@code connected} is
 * true. Exposing the refresh token here is intentional, not an oversight: this endpoint is
 * called server-to-server only, by the Next.js frontend's own server (which needs the refresh
 * token back so it can proactively refresh the access token before it expires and push the
 * result back via {@code PUT}). The browser/client JS never sees this DTO directly — the
 * frontend exposes a separate, public-facing route that strips these two fields before
 * anything reaches client-side code. Do not reuse this DTO for a browser-facing response.
 */
public record GoogleHealthConnectionStatusDto(
        @Schema(requiredMode = REQUIRED) boolean connected, Instant expiresAt, String refreshToken) {

    public static GoogleHealthConnectionStatusDto disconnected() {
        return new GoogleHealthConnectionStatusDto(false, null, null);
    }
}
