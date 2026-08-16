package org.reciplease.dto;

/**
 * Response of {@code POST /api/auth/exchange}, {@code POST /api/auth/refresh}, and the passkey
 * signup/login finish endpoints. {@code refreshToken} is only set on a fresh login/signup (not
 * when linking an additional identity to an already-authenticated session), since linking
 * doesn't establish a new session.
 */
public record ExchangeResponse(String token, String refreshToken, String userId, String handle) {}
