package org.reciplease.model;

/**
 * A resolved member of a house, joined against the user's email so callers never
 * have to deal with raw Google subject ids without context.
 */
public record HouseMembership(String userId, Email email, HouseRole role) {
}
