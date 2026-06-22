package org.reciplease.dto;

/**
 * Body of {@code POST /api/auth/exchange}. {@code linkToken} is an existing Reciplease JWT,
 * present only when linking a second provider identity to an already-authenticated user.
 */
public record ExchangeRequest(String provider, String providerId, String linkToken) {}
