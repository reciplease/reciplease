package org.reciplease.service;

/** Thrown when a Fitbit API call is attempted for a user with no linked Fitbit account. */
public class FitbitNotConnectedException extends RuntimeException {
    public FitbitNotConnectedException(final String userId) {
        super("User " + userId + " has no linked Fitbit account");
    }
}
