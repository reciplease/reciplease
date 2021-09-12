package org.reciplease.repository;

import org.reciplease.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {
    List<InventoryItem> findByExpirationIsGreaterThanEqual(LocalDate now);

    List<InventoryItem> findByExpirationIsBefore(LocalDate now);
}
