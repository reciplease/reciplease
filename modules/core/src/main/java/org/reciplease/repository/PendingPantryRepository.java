package org.reciplease.repository;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.PendingPantryItem;

public interface PendingPantryRepository {
    Optional<PendingPantryItem> findById(String id);

    PendingPantryItem save(PendingPantryItem item);

    List<PendingPantryItem> findAllByHouseId(String houseId);

    void deleteById(String id);
}
