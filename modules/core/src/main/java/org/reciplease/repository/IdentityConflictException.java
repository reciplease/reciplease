package org.reciplease.repository;

/**
 * Thrown when attempting to link a (provider, providerId) identity that is already
 * linked to a different user.
 */
public class IdentityConflictException extends RuntimeException {
    public IdentityConflictException(final String provider, final String providerId) {
        super("Identity " + provider + ":" + providerId + " is already linked to a different user");
    }
}
