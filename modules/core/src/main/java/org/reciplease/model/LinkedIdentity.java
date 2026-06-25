package org.reciplease.model;

/**
 * A login-provider identity linked to a user, as exposed to the user themselves —
 * the provider's email (so they can tell their accounts apart), never the opaque
 * provider id.
 */
public record LinkedIdentity(String provider, String email) {}
