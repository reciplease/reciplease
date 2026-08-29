package org.reciplease.service;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.FoodCatalogEntry;

/**
 * Looks up foods (and their macros) from an external catalog, by name or barcode. Best-effort:
 * a down/unreachable provider returns an empty result rather than throwing, so callers combining
 * multiple sources degrade gracefully.
 */
public interface FoodCatalogPort {

    List<FoodCatalogEntry> searchByName(String query);

    Optional<FoodCatalogEntry> lookupByBarcode(String barcode);
}
