package org.reciplease.repository;

import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    Optional<InventoryItem> findByUuid(final UUID uuid);

    InventoryItem save(final InventoryItem item);

    List<InventoryItem> findAll();

    List<InventoryItem> expiresAfter(LocalDate now);

    List<InventoryItem> betweenDates(LocalDate now);
}
