package org.reciplease.repository;

import org.reciplease.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
}
