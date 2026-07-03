package org.reciplease.service;

/** Thrown when a Google Health API call is attempted for a user with no linked Google Health account. */
public class GoogleHealthNotConnectedException extends RuntimeException {
    public GoogleHealthNotConnectedException(final String userId) {
        super("User " + userId + " has no linked Google Health account");
    }
}
