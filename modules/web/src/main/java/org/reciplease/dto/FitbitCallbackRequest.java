package org.reciplease.dto;

/**
 * Body of {@code POST /api/fitbit/callback}. {@code code} is the authorization code from the
 * frontend-driven Fitbit OAuth2/PKCE redirect; {@code codeVerifier} is the PKCE verifier the
 * frontend generated for that same flow.
 */
public record FitbitCallbackRequest(String code, String codeVerifier) {}
