package org.reciplease.repository;

import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// extends JpaRepository<InventoryItem, UUID>
public interface InventoryRepository {
    Optional<InventoryItem> findById(final UUID uuid);
    InventoryItem save(final InventoryItem item);
    List<InventoryItem> findAll();
    List<InventoryItem> findByExpirationIsGreaterThanEqual(LocalDate now);
    List<InventoryItem> findByExpirationIsBefore(LocalDate now);
}
