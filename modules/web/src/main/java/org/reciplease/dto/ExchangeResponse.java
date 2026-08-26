package org.reciplease.dto;

import java.time.Instant;

/**
 * Response of {@code POST /api/auth/exchange}, {@code POST /api/auth/refresh}, and the passkey
 * signup/login finish endpoints. {@code refreshToken} (and its {@code refreshTokenExpiresAt}) is
 * only set on a fresh login/signup or a rotation (not when linking an additional identity to an
 * already-authenticated session), since linking doesn't establish a new session. Callers use
 * {@code refreshTokenExpiresAt} as the source of truth for the refresh token's actual lifetime
 * (see {@code reciplease.jwt.refresh-token-ttl}) rather than hardcoding a duplicate of it.
 */
public record ExchangeResponse(String token, String refreshToken, Instant refreshTokenExpiresAt, String userId, String handle) {}
