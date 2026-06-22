package org.reciplease.repository;

/** Thrown when attempting to set a handle that another user already has. */
public class HandleTakenException extends RuntimeException {
    public HandleTakenException(final String handle) {
        super("Handle already taken: " + handle);
    }
}
