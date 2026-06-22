package org.reciplease.model;

/**
 * A single login-provider identity linked to a {@link User} — e.g. a Google or GitHub
 * account. {@code providerId} is that provider's stable subject/user id, opaque outside
 * this provider.
 */
public record Identity(String provider, String providerId) {}
