package org.reciplease.repository;

import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.List;

// extends JpaRepository<InventoryItem, UUID>
public interface InventoryRepository {
    List<InventoryItem> findByExpirationIsGreaterThanEqual(LocalDate now);
    List<InventoryItem> findByExpirationIsBefore(LocalDate now);
}
