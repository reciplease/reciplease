package org.reciplease.dto;

/** Response of {@code POST /api/auth/exchange} and the {@code POST /api/me/handle} update. */
public record ExchangeResponse(String token, String userId, String handle) {}
