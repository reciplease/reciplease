package org.reciplease.model;

/**
 * A user's email address, typed so callers can't accidentally pass a raw string
 * (a user id, a house name, ...) where an email is expected.
 */
public record Email(String value) {

    @Override
    public String toString() {
        return value;
    }
}
