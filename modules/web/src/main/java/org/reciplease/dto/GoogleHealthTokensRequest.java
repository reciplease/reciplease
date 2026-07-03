package org.reciplease.dto;

/**
 * Body of {@code PUT /api/google-health/connection}. Carries access/refresh tokens the
 * frontend already obtained itself — either from the initial Google OAuth2 authorization-code
 * exchange, or from a subsequent refresh — for the backend to persist as-is. The backend never
 * performs the OAuth2 exchange or refresh itself and never holds the Google OAuth client
 * secret; see {@link org.reciplease.service.GoogleHealthService}.
 */
public record GoogleHealthTokensRequest(String accessToken, String refreshToken, long expiresIn, String scope) {}
