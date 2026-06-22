package org.reciplease.model;

/**
 * An authenticated, allowlisted Reciplease user. The {@code id} is an internally
 * generated stable identifier (not tied to any single login provider); a user may
 * have multiple linked provider {@link Identity}s. {@code handle} is a user-chosen
 * display name and may be {@code null} until the user sets one.
 */
public record User(String id, String handle) implements Identifiable {}
