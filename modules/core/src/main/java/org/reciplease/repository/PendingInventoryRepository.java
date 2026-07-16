package org.reciplease.repository;

import org.reciplease.model.PendingInventoryItem;

import java.util.List;
import java.util.Optional;

public interface PendingInventoryRepository {
    Optional<PendingInventoryItem> findById(String id);

    PendingInventoryItem save(PendingInventoryItem item);

    List<PendingInventoryItem> findAllByHouseId(String houseId);

    void deleteById(String id);
}
