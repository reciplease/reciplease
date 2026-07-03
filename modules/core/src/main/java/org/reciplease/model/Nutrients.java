package org.reciplease.model;

/**
 * Macro/energy values for a food, as reported by whichever catalog produced them. Fields are
 * nullable since not every provider (or every product within a provider) reports all of them.
 */
public record Nutrients(Double energyKcal, Double proteinG, Double fatG, Double carbohydrateG) {}
