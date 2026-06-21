package org.reciplease.model;

/**
 * A user's permission level within a house. {@code OWNER}s can create, edit, and
 * delete house-scoped data; {@code READ_ONLY} members can only view it.
 */
public enum HouseRole {
    OWNER,
    READ_ONLY
}
