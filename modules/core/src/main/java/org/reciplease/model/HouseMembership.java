package org.reciplease.model;

/**
 * A resolved member of a house, joined against the user's handle so callers never
 * have to deal with raw user ids without context. {@code handle} may be {@code null}
 * if the member hasn't set one yet.
 */
public record HouseMembership(String userId, String handle, HouseRole role) {
}
