package org.reciplease.repository;

import org.reciplease.model.PendingPantryItem;

import java.util.List;
import java.util.Optional;

public interface PendingPantryRepository {
    Optional<PendingPantryItem> findById(String id);

    PendingPantryItem save(PendingPantryItem item);

    List<PendingPantryItem> findAllByHouseId(String houseId);

    void deleteById(String id);
}
