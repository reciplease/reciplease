package org.reciplease.repository;

import org.reciplease.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {
}
