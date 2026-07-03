package org.reciplease.model;

/** A food the user has previously logged, surfaced back for fuzzy-matching against a new query. */
public record LoggedFoodHistoryEntry(String consumptionId, String displayName) {}
