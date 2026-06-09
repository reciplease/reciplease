package org.reciplease.model;

/**
 * An authenticated, allowlisted Reciplease user. The {@code id} is the stable Google
 * subject identifier from the JWT {@code sub} claim.
 */
public record User(String id, String email) implements Identifiable {}
