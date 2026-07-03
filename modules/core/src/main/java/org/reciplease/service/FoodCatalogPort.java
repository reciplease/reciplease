package org.reciplease.service;

import org.reciplease.model.FoodCatalogEntry;

import java.util.List;
import java.util.Optional;

/**
 * Looks up foods (and their macros) from an external catalog, by name or barcode. Best-effort:
 * a down/unreachable provider returns an empty result rather than throwing, so callers combining
 * multiple sources degrade gracefully.
 */
public interface FoodCatalogPort {

    List<FoodCatalogEntry> searchByName(String query);

    Optional<FoodCatalogEntry> lookupByBarcode(String barcode);
}
