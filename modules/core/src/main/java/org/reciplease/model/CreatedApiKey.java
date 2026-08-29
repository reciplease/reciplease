package org.reciplease.model;

/**
 * The result of minting a new {@link ApiKey}: the persisted record plus the one-time raw
 * secret, which is never stored and cannot be recovered once this response is sent.
 */
public record CreatedApiKey(ApiKey apiKey, String rawKey) {}
