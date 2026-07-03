package org.reciplease.model;

/**
 * A candidate food returned by a {@link org.reciplease.service.FoodCatalogPort} search or
 * barcode lookup. {@code sourceId} is an opaque identifier the catalog adapter that produced
 * this entry understands — core never interprets it, it's only threaded back through on log.
 */
public record FoodCatalogEntry(String sourceId, String displayName, String brand, Nutrients nutrients) {}
